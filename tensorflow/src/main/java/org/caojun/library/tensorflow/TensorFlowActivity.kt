/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package org.caojun.library.tensorflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import org.caojun.library.tensorflow.view.AutoFitTextureView
import org.caojun.library.tensorflow.model.TensorFlowObjectDetectionAPIModel
import java.io.IOException
import java.lang.Long
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * 本识别是专门用来识别抽烟、打电话、睁眼、闭眼的
 */
class TensorFlowActivity : AppCompatActivity() {
    private val lock = Any()
    private var runClassifier = false
//    private var textView: TextView? = null
//    private var imageView: ImageView? = null
    private var classifier: Classifier? = null

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a [ ].
     */
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

    /**
     * ID of the current [CameraDevice].
     */
    private var cameraId = "0"

    /**
     * An [AutoFitTextureView] for camera preview.
     */
//    private var textureView: AutoFitTextureView? = null

    /**
     * A [CameraCaptureSession] for camera preview.
     */
    private var captureSession: CameraCaptureSession? = null

    /**
     * A reference to the opened [CameraDevice].
     */
    private var cameraDevice: CameraDevice? = null

    /**
     * The [Size] of camera preview.
     */
    private var previewSize: Size? = null

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
     */
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {

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
            finish()
        }
    }

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null

    /**
     * An [ImageReader] that handles image capture.
     */
    private var imageReader: ImageReader? = null

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    /**
     * [CaptureRequest] generated by [.previewRequestBuilder]
     */
//    private var previewRequest: CaptureRequest? = null

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tfeyes)

        XXPermissions.with(this)
            .permission(Permission.CAMERA)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    try {
                        // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
                        classifier = TensorFlowObjectDetectionAPIModel.create(
                            this@TensorFlowActivity.assets, TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE
                        )
                    } catch (e: IOException) {
                    }
                    doResume()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    finish()
                }
            })
    }

    public override fun onResume() {
        super.onResume()
        doResume()
    }

    private fun doResume() {
        if (classifier == null) {
            return
        }
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        val textureView = findViewById<TextureView>(R.id.textureView)
        val imageView = findViewById<ImageView>(R.id.imageView)
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
            imageView.layoutParams.width = textureView.width
            imageView.layoutParams.height = textureView.height
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    public override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    public override fun onDestroy() {
        classifier?.close()
        super.onDestroy()
    }

    /**
     * 设置相机参数
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height, ImageFormat.JPEG,  /*maxImages*/2
                )

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = windowManager.defaultDisplay.rotation
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
                windowManager.defaultDisplay.getSize(displaySize)
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

                val textureView = findViewById<AutoFitTextureView>(R.id.textureView)
                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize!!.width, previewSize!!.height)
                } else {
                    textureView.setAspectRatio(previewSize!!.height, previewSize!!.width)
                }

                //修改摄像头方向  0后置摄像头  1前置摄像头
//                this.cameraId = "1"
                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private fun openCamera(width: Int, height: Int) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        imageView.layoutParams.width = width
        imageView.layoutParams.height = height
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
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

    /**
     * 关闭当前摄像头
     */
    @SuppressLint("NewApi")
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

    /**
     * 启动后台线程
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
        synchronized(lock) { runClassifier = true }
        backgroundHandler?.post(periodicClassify)
    }

    /**
     * 停止后台线程
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
            synchronized(lock) { runClassifier = false }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 定期拍照并识别
     */
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

    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            if (previewSize == null) {
                return
            }
            val textureView = findViewById<TextureView>(R.id.textureView)
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
                        // The camera is already closed
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
//                            captureSession?.setRepeatingRequest(
//                                previewRequest!!, captureCallback, backgroundHandler
//                            )
                            captureSession?.setRepeatingRequest(
                                previewRequest!!, null, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
//                        showToast("Failed")
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Configures the necessary [Matrix] transformation to `textureView`. This
     * method should be called after the camera preview size is determined in setUpCameraOutputs and
     * also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val textureView = findViewById<TextureView>(R.id.textureView)
        if (null == textureView || null == previewSize) {
            return
        }
        val rotation = windowManager.defaultDisplay.rotation
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

    /**
     * tensorFlow识别
     */
    private fun classifyFrame() {
        if (classifier == null || cameraDevice == null) {
            return
        }
        val textureView = findViewById<TextureView>(R.id.textureView)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val bitmap = textureView.getBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE)
        val results = classifier?.recognizeImage(bitmap)
        canvasWidth = textureView.width.toFloat()
        canvasHeight = textureView.height.toFloat()
        if (textureView.width != imageView.width || textureView.height != imageView.height) {
            canvasWidth = textureView.width.toFloat()
            canvasHeight = textureView.height.toFloat()
            imageView.layoutParams.width = textureView.width
            imageView.layoutParams.height = textureView.height
        }
        bitmap?.recycle()
        val croppedBitmap = Bitmap.createBitmap(
            canvasWidth.toInt(), canvasHeight.toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(croppedBitmap)


//        for (final Classifier.Recognition result : results) {
        for (i in results!!.indices) {
            val result = results[i]
            val location = result?.getLocation()
            //            Log.d("eyes", "---------------------------------------------");
            if (location != null && result.confidence!! >= MINIMUM_CONFIDENCE_TF_OD_API) {
                val paint = Paint()
                val paint1 = Paint()
                when (result.title) {
                    "openeyes" -> {
                        paint.color = Color.GREEN
                        paint1.color = Color.GREEN
                        //                    Log.d("eyes", i + ".openeyes.left: " + location.left);
            //                    Log.d("eyes", i + ".openeyes.id: " + result.getId());
                    }
                    "closeeyes" -> {
                        paint.color = Color.RED
                        paint1.color = Color.RED
                        //                    Log.d("eyes", i + ".closeeyes.left: " + location.left);
            //                    Log.d("eyes", i + ".closeeyes.id: " + result.getId());
                    }
                    "phone" -> {
                        paint.color = -0x6700
                        paint1.color = -0x6700
                    }
                    "smoke" -> {
                        paint.color = Color.YELLOW
                        paint1.color = Color.YELLOW
                    }
                    else -> paint.color = Color.WHITE
                }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5.0f
                paint.isAntiAlias = true
                paint1.style = Paint.Style.FILL
                paint1.alpha = 125
                //                canvas.drawRect(location, paint);
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
        imageView.post { imageView.setImageBitmap(croppedBitmap) }
    }

    /**
     * Compares two `Size`s based on their areas.
     */
    private class CompareSizesByArea : Comparator<Size> {

        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height
            )
        }
    }

    companion object {
        //    /**
        //     * Tag for the {@link Log}.
        //     */
        //    private static final String TAG = "TfLiteCameraDemo";
//        private const val FRAGMENT_DIALOG = "dialog"
        private const val HANDLE_THREAD_NAME = "CameraBackground"
//        private const val PERMISSIONS_REQUEST_CODE = 1
        private const val TF_OD_API_MODEL_FILE =
            "file:///android_asset/frozen_inference_graph_v6.pb"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt"
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.6f
        private var canvasWidth = 100f
        private var canvasHeight = 100f

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private const val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private const val MAX_PREVIEW_HEIGHT = 1080

        /**
         * Resizes image.
         *
         *
         * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
         * resulting in gorgeous previews but the storage of garbage capture data.
         *
         *
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that is
         * at least as large as the respective texture view size, and that is at most as large as the
         * respective max size, and whose aspect ratio matches with the specified value. If such size
         * doesn't exist, choose the largest one that is at most as large as the respective max size, and
         * whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended output class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
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
        //            Log.e(TAG, "Couldn't find any suitable preview size");
                    choices[0]
                }
            }
        }
    }
}