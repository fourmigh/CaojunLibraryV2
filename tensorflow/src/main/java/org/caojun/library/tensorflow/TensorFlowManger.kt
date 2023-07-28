package org.caojun.library.tensorflow

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import org.caojun.library.tensorflow.model.TensorFlowObjectDetectionAPIModel
import org.caojun.library.tensorflow.view.AutoFitTextureView
import java.io.IOException
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class TensorFlowManger(private val activity: Activity,
                       private val listener: Listener,
                       private val textureView: AutoFitTextureView,
                       private val imageView: ImageView? = null,
                       private val cameraId: String = "0") {

    companion object {
        private const val HANDLE_THREAD_NAME = "CameraBackground"
        private const val TF_OD_API_MODEL_FILE =
            "file:///android_asset/frozen_inference_graph_v6.pb"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt"
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.6f

        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080

        //超过次数未检测到，last值设为null
        private const val MAX_COUNT_EYE = 10

        private const val INDEX_LEFT = 0
        private const val INDEX_RIGHT = 1
        private const val INDEX_OPEN = 0
        private const val INDEX_CLOSE = 1
        private const val EYE_OPEN = "openeyes"
        private const val EYE_CLOSE = "closeeyes"
        //下一次眨眼时间范围
        private val EYE_TIME = arrayOf(3000L, 5000L)
        //连续两次眨眼间隔
        private val EYE_INTERVAL = arrayOf(200L, 500L)
        //眨眼间隔
        private val BLINK_INTERVAL = arrayOf(100L, 500L)
    }

    //0左 1右
    private var countEyes = 0
    //0open 1close
    private val lastEyeLeft: Array<Boolean?> = arrayOf(null, null)
    private val lastEyeRight: Array<Boolean?> = arrayOf(null, null)

    interface Listener {
        fun onEye(left: Boolean, open: Boolean)
        fun onDisappear()
        fun onError()
        fun onMouth(open: Boolean)
        fun onSpeakOver()
    }

    private class CompareSizesByArea : Comparator<Size> {

        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(
                lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height
            )
        }
    }

    private var canvasWidth = 100f
    private var canvasHeight = 100f
    private val cameraOpenCloseLock = Semaphore(1)
//    private var cameraId = "0"
    private var previewSize: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var backgroundHandler: Handler? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var classifier: Classifier? = null
    private var backgroundThread: HandlerThread? = null
    private val lock = Any()
    private var runClassifier = false
    //模拟眨眼
    private var simulateRunning = false
    //上一次眨眼时间
    private var lastTime = 0L
    //下一次眨眼时间
    private var nextTime = 0L

    private val surfaceTextureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(
                texture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera(width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                texture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                configureTransform(width, height)
            }

            override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
        }

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(currentCameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release()
            cameraDevice = currentCameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            currentCameraDevice.close()
            cameraDevice = null
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            currentCameraDevice.close()
            cameraDevice = null
            listener.onError()
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == previewSize) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
            0F, 0F, previewSize!!.height
                .toFloat(), previewSize!!.width.toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale =
                (viewHeight.toFloat() / previewSize!!.height).coerceAtLeast(viewWidth.toFloat() / previewSize!!.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        imageView?.layoutParams?.width = width
        imageView?.layoutParams?.height = height
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            if (previewSize == null) {
                return
            }
            val texture = textureView.surfaceTexture

            // We configure the size of default buffer to be the size of camera preview we want.
            texture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder =
                cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (null == cameraDevice) {
                            return
                        }

                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder?.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )

                            // Finally, we start displaying the camera preview.
                            val previewRequest = previewRequestBuilder?.build()
                            captureSession?.setRepeatingRequest(
                                previewRequest!!, null, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough: MutableList<Size> = ArrayList()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return when {
            bigEnough.size > 0 -> {
                Collections.min(bigEnough, CompareSizesByArea())
            }
            notBigEnough.size > 0 -> {
                Collections.max(notBigEnough, CompareSizesByArea())
            }
            else -> {
                choices[0]
            }
        }
    }

    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
//                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
//                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    continue
//                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue

                // // For still image captures, we use the largest available size.
                val largest = Collections.max(
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height, ImageFormat.JPEG,  /*maxImages*/2
                )

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = activity.windowManager.defaultDisplay.rotation
                // noinspection ConstantConditions
                /* Orientation of the camera sensor */
                val sensorOrientation = characteristics.get(
                    CameraCharacteristics.SENSOR_ORIENTATION
                )!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                }
                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth,
                    rotatedPreviewHeight,
                    maxPreviewWidth,
                    maxPreviewHeight,
                    largest
                )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = activity.resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize!!.width, previewSize!!.height)
                } else {
                    textureView.setAspectRatio(previewSize!!.height, previewSize!!.width)
                }

                //修改摄像头方向  0后置摄像头  1前置摄像头
//                this.cameraId = "1"
//                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private val periodicClassify: Runnable = object : Runnable {
        override fun run() {
            synchronized(lock) {
                if (runClassifier) {
                    classifyFrame()
                }
            }
            backgroundHandler?.post(this)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
        synchronized(lock) { runClassifier = true }
        backgroundHandler?.post(periodicClassify)
    }

    private fun classifyFrame() {
        if (classifier == null || cameraDevice == null) {
            return
        }
        val bitmap = textureView.getBitmap(
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE
        )
        val results = classifier?.recognizeImage(bitmap)
        canvasWidth = textureView.width.toFloat()
        canvasHeight = textureView.height.toFloat()
        if (textureView.width != imageView?.width || textureView.height != imageView.height) {
            canvasWidth = textureView.width.toFloat()
            canvasHeight = textureView.height.toFloat()
            imageView?.layoutParams?.width = textureView.width
            imageView?.layoutParams?.height = textureView.height
        }
        bitmap?.recycle()
        val croppedBitmap = Bitmap.createBitmap(
            canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(croppedBitmap)


//        for (final Classifier.Recognition result : results) {

        countEyes --
        var eyeRecognize = false
        for (i in results!!.indices) {
            val result = results[i]
            val location = result.getLocation()

            if (result.confidence == null) {
                continue
            }

            if (result.confidence >= MINIMUM_CONFIDENCE_TF_OD_API) {
                val paint = Paint()
                val paint1 = Paint()
                if (result.title == EYE_OPEN || result.title == EYE_CLOSE) {
                    eyeRecognize = true
                }
                Log.d("TensorFlowManger", "result.title: ${result.title}")
                when (result.title) {
                    EYE_OPEN -> {
                        paint.color = Color.GREEN
                        paint1.color = Color.GREEN

                        when (i) {
                            INDEX_LEFT -> {
                                if (lastEyeLeft[INDEX_OPEN] != true) {
                                    lastEyeLeft[INDEX_OPEN] = true
                                    lastEyeLeft[INDEX_CLOSE] = false
                                    simulateRunning = false
                                    activity.runOnUiThread {
                                        listener.onEye(left = true, open = true)
                                    }
                                }
                            }
                            INDEX_RIGHT -> {
                                if (lastEyeRight[INDEX_OPEN] != true) {
                                    lastEyeRight[INDEX_OPEN] = true
                                    lastEyeRight[INDEX_CLOSE] = false
                                    simulateRunning = false
                                    activity.runOnUiThread {
                                        listener.onEye(left = false, open = true)
                                    }
                                }
                            }
                        }
                    }
                    EYE_CLOSE -> {
                        paint.color = Color.RED
                        paint1.color = Color.RED

                        when (i) {
                            INDEX_LEFT -> {
                                if (lastEyeLeft[INDEX_CLOSE] != true) {
                                    lastEyeLeft[INDEX_CLOSE] = true
                                    lastEyeLeft[INDEX_OPEN] = false
                                    simulateRunning = false
                                    activity.runOnUiThread {
                                        listener.onEye(left = true, open = false)
                                    }
                                }
                            }
                            INDEX_RIGHT -> {
                                if (lastEyeRight[INDEX_CLOSE] != true) {
                                    lastEyeRight[INDEX_CLOSE] = true
                                    lastEyeRight[INDEX_OPEN] = false
                                    simulateRunning = false
                                    activity.runOnUiThread {
                                        listener.onEye(left = false, open = false)
                                    }
                                }
                            }
                        }
                    }
//                    "phone" -> {
//                        paint.color = -0x6700
//                        paint1.color = -0x6700
//                    }
//                    "smoke" -> {
//                        paint.color = Color.YELLOW
//                        paint1.color = Color.YELLOW
//                    }
                    else -> {
                        paint.color = Color.WHITE
                    }
                }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5.0f
                paint.isAntiAlias = true
                paint1.style = Paint.Style.FILL
                paint1.alpha = 125
                canvas.drawRect(
                    canvasWidth * location.left / TF_OD_API_INPUT_SIZE,
                    canvasHeight * location.top / TF_OD_API_INPUT_SIZE,
                    canvasWidth * location.right / TF_OD_API_INPUT_SIZE,
                    canvasHeight * location.bottom / TF_OD_API_INPUT_SIZE, paint
                )
                canvas.drawRect(
                    canvasWidth * location.left / TF_OD_API_INPUT_SIZE,
                    canvasHeight * location.top / TF_OD_API_INPUT_SIZE,
                    canvasWidth * location.right / TF_OD_API_INPUT_SIZE,
                    canvasHeight * location.bottom / TF_OD_API_INPUT_SIZE, paint1
                )
            }
        }
        imageView?.post { imageView.setImageBitmap(croppedBitmap) }

        if (eyeRecognize) {
            countEyes = MAX_COUNT_EYE
        } else  if (countEyes == 0) {
            lastEyeLeft[INDEX_OPEN] = null
            lastEyeLeft[INDEX_CLOSE] = null
            lastEyeRight[INDEX_OPEN] = null
            lastEyeRight[INDEX_CLOSE] = null
            activity.runOnUiThread {
                startSimulate()
                listener.onDisappear()
            }
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            synchronized(lock) { runClassifier = false }
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun onCreate() {
        XXPermissions.with(activity)
            .permission(Permission.CAMERA)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    try {
                        // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
                        classifier = TensorFlowObjectDetectionAPIModel.create(
                            activity.assets,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE
                        )
                    } catch (e: IOException) {
                    }
                    onResume()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    listener.onError()
                }
            })
    }

    private fun getRandom(startNum: Long, endNum: Long): Long {
        if (endNum > startNum) {
            val random = Random()
            val next = random.nextInt((endNum - startNum).toInt())
            return next.toLong()
        }
        return endNum
    }

    private fun getRandomTwice(): Boolean {
        return getRandom(0, 100L) < 10
    }

    fun startSimulate() {
        SimulateThread().start()
    }
    private inner class SimulateThread : Thread() {
        override fun run() {
            simulateRunning = true
            lastTime = System.currentTimeMillis()
            nextTime = getRandom(EYE_TIME[0], EYE_TIME[1])
            while (simulateRunning) {
                var isBlink = false
                if (System.currentTimeMillis() - lastTime >= nextTime) {
                    isBlink = true
                    //眨眼
                    if (getRandomTwice()) {
                        //两次
                        blink()
                        val time = getRandom(EYE_INTERVAL[0], EYE_INTERVAL[1])
                        sleep(time)
                        blink()
                    } else {
                        blink()
                    }
                }
                sleep(50)
                if (isBlink) {
                    lastTime = System.currentTimeMillis()
                    nextTime = getRandom(EYE_TIME[0], EYE_TIME[1])
                }
            }
        }
    }

    private fun blink() {
        activity.runOnUiThread {
            listener.onEye(left = true, open = false)
            listener.onEye(left = false, open = false)
        }
        val time = getRandom(BLINK_INTERVAL[0], BLINK_INTERVAL[1])
        SystemClock.sleep(time)
        activity.runOnUiThread {
            listener.onEye(left = true, open = true)
            listener.onEye(left = false, open = true)
        }
    }

    fun onResume() {
        if (classifier == null) {
            return
        }
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
            imageView?.layoutParams?.width = textureView.width
            imageView?.layoutParams?.height = textureView.height
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    fun onPause() {
        closeCamera()
        stopBackgroundThread()
    }

    fun onDestroy() {
        simulateRunning = false
        classifier?.close()
    }

    //嘴
    fun startSpeak(text: String?, timeSleep: Long = 200) {
        stopSpeak()

        countWords = text?.length ?: 0
        if (countWords > 0) {
            SpeakThread(timeSleep).start()
        }
    }
    fun stopSpeak() {
        countWords = 0
        activity.runOnUiThread {
            listener.onMouth(false)
        }
    }
    private var countWords = 0
    private inner class SpeakThread(private val timeSleep: Long) : Thread() {
        override fun run() {
            var open = true
            while (countWords > 0) {
                activity.runOnUiThread {
                    listener.onMouth(open)
                }
                open = !open
                try {
                    sleep(timeSleep)
                } catch (e: Exception) {
                }
                countWords --
            }
            stopSpeak()
            listener.onSpeakOver()
        }
    }

    fun isSpeaking(): Boolean {
        return countWords > 0
    }
}