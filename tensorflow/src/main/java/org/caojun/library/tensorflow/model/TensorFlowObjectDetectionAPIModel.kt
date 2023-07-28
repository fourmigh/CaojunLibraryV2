/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

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
package org.caojun.library.tensorflow.model

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import org.caojun.library.tensorflow.Classifier
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import org.caojun.library.tensorflow.Classifier.Recognition
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.util.*

/**
 * 检测图形并且绘制（规则图形矩形）
 */
class TensorFlowObjectDetectionAPIModel : Classifier {
    // Config values.
    private var inputName: String? = null
    private var inputSize = 0

    // Pre-allocated buffers.
    private val labels = Vector<String>()
    private lateinit var intValues: IntArray
    private lateinit var byteValues: ByteArray
    private lateinit var outputLocations: FloatArray
    private lateinit var outputScores: FloatArray
    private lateinit var outputClasses: FloatArray
    private lateinit var outputNumDetections: FloatArray
    private lateinit var outputNames: Array<String>
//    private var logStats = false
    private lateinit var inferenceInterface: TensorFlowInferenceInterface

    override fun recognizeImage(bitmap: Bitmap?): List<Recognition> {
        // Log this method so that it can be analyzed with systrace.
//        Trace.beginSection("recognizeImage")
//        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in intValues.indices) {
            byteValues[i * 3 + 2] = (intValues[i] and 0xFF).toByte()
            byteValues[i * 3 + 1] = (intValues[i] shr 8 and 0xFF).toByte()
            byteValues[i * 3 + 0] = (intValues[i] shr 16 and 0xFF).toByte()
        }
//        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
//        Trace.beginSection("feed")
        inferenceInterface.feed(
            inputName,
            byteValues,
            1,
            inputSize.toLong(),
            inputSize.toLong(),
            3
        )
//        Trace.endSection()

        // Run the inference call.
//        Trace.beginSection("run")
        inferenceInterface.run(outputNames, false)
//        Trace.endSection()

        // Copy the output Tensor back into the output array.
//        Trace.beginSection("fetch")
        outputLocations = FloatArray(MAX_RESULTS * 4)
        outputScores = FloatArray(MAX_RESULTS)
        outputClasses = FloatArray(MAX_RESULTS)
        outputNumDetections = FloatArray(1)
        inferenceInterface.fetch(outputNames[0], outputLocations)
        inferenceInterface.fetch(outputNames[1], outputScores)
        inferenceInterface.fetch(outputNames[2], outputClasses)
        inferenceInterface.fetch(outputNames[3], outputNumDetections)
//        Trace.endSection()

        // Find the best detections.
        val pq = PriorityQueue<Recognition>(
            1
        ) { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
            (rhs.confidence!!).compareTo(lhs.confidence!!)
        }

        // Scale them back to the input size.
        for (i in outputScores.indices) {
            val detection = RectF(
                outputLocations[4 * i + 1] * inputSize,
                outputLocations[4 * i] * inputSize,
                outputLocations[4 * i + 3] * inputSize,
                outputLocations[4 * i + 2] * inputSize
            )
            val indexLabels = outputClasses[i].toInt()
            pq.add(
                Recognition("" + i, labels[indexLabels], outputScores[i], detection)
            )
        }
        val recognitions = ArrayList<Recognition>()
        for (i in 0 until pq.size.coerceAtMost(MAX_RESULTS)) {
            recognitions.add(pq.poll())
        }
//        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override val statString: String
        get() = inferenceInterface.statString

    override fun close() {
        inferenceInterface.close()
    }

    companion object {
        //  private static final Logger LOGGER = new Logger();
        // Only return this many results.
        private const val MAX_RESULTS = 100

        /**
         * Initializes a native TensorFlow session for classifying images.
         *
         * @param assetManager  The asset manager to be used to load assets.
         * @param modelFilename The filepath of the model GraphDef protocol buffer.
         * @param labelFilename The filepath of label file for classes.
         */
        @Throws(IOException::class)
        fun create(
            assetManager: AssetManager,
            modelFilename: String?,
            labelFilename: String,
            inputSize: Int
        ): Classifier {
            val d = TensorFlowObjectDetectionAPIModel()
            val labelsInput: InputStream?
            val actualFilename = labelFilename.split("file:///android_asset/").toTypedArray()[1]
            labelsInput = assetManager.open(actualFilename)
            val br: BufferedReader?
            br = BufferedReader(InputStreamReader(labelsInput))
            var line: String?
            while (true) {
                line = br.readLine()
                if (line == null) {
                    break
                }
                d.labels.add(line)
            }
            br.close()
            d.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)
            val g = d.inferenceInterface.graph()
            d.inputName = "image_tensor"
            // The inputName node has a shape of [N, H, W, C], where
            // N is the batch size
            // H = W are the height and width
            // C is the number of channels (3 for our purposes - RGB)
            val inputOp = g.operation(d.inputName)
                ?: throw RuntimeException("Failed to find input Node '" + d.inputName + "'")
            d.inputSize = inputSize
            // The outputScoresName node has a shape of [N, NumLocations], where N
            // is the batch size.
            val outputOp1 = g.operation("detection_scores")
                ?: throw RuntimeException("Failed to find output Node 'detection_scores'")
            val outputOp2 = g.operation("detection_boxes")
                ?: throw RuntimeException("Failed to find output Node 'detection_boxes'")
            val outputOp3 = g.operation("detection_classes")
                ?: throw RuntimeException("Failed to find output Node 'detection_classes'")

            // Pre-allocate buffers.
            d.outputNames = arrayOf(
                "detection_boxes", "detection_scores",
                "detection_classes", "num_detections"
            )
            d.intValues = IntArray(d.inputSize * d.inputSize)
            d.byteValues = ByteArray(d.inputSize * d.inputSize * 3)
            d.outputScores = FloatArray(MAX_RESULTS)
            d.outputLocations = FloatArray(MAX_RESULTS * 4)
            d.outputClasses = FloatArray(MAX_RESULTS)
            d.outputNumDetections = FloatArray(1)
            return d
        }
    }
}