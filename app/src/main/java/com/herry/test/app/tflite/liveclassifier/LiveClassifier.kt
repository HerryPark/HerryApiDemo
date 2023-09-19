package com.herry.test.app.tflite.liveclassifier

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteOrder

class LiveClassifier {

    companion object {
        // https://tfhub.dev/tensorflow/lite-model/efficientnet/lite4/fp32/2
        // TFLite (efficientnet/lite4/fp32) // "efficientnet_lite4_fp32_2.tflite"
        private const val MODEL_NAME = "mobilenet_imagenet_model.tflite"
        private const val LABEL_FILE = "labels.txt"
    }

    data class Classified(
        val what: String,
        val accuracy: Float
    )

    private var interpreter: Interpreter? = null
    private var modelInputChannel: Int = 0

    private var inputTensorDataType: DataType? = null
    private var imageProcessor: ImageProcessor? = null
    private var labels: MutableList<String> = mutableListOf()

    @WorkerThread
    @Throws(Exception::class)
    fun classify(context: Context, image: Bitmap): Classified  {
        if (image.width <= 0 || image.height <= 0) {
            throw NullPointerException()
        }

        val interpreter = interpreter ?: (Interpreter(FileUtil.loadMappedFile(context, MODEL_NAME).apply {
            this.order(ByteOrder.nativeOrder())
        }).apply {
            // initialize model shape
            initModelShape(context, this)
        }.also {
            this.interpreter = it
        })

        val inputTensorDataType = this.inputTensorDataType
        val imageProcessor = this.imageProcessor
        val labels = this.labels

        if (inputTensorDataType == null || imageProcessor == null || labels.isEmpty()) {
            throw NullPointerException()
        }

        val outputTensor = interpreter.getOutputTensor(0)
        val outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())

        val inputImage = loadImage(inputTensorDataType, imageProcessor, image)
        interpreter.run(inputImage.buffer, outputBuffer.buffer.rewind())

        val output: Map<String, Float> = TensorLabel(labels, outputBuffer).mapWithFloatValue

        val classified = argmax(output)

        return Classified(classified.first, classified.second)
    }

    private fun initModelShape(context: Context, interpreter: Interpreter) {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()

        modelInputChannel = inputShape[0]
        val modelInputWidth = inputShape[1]
        val modelInputHeight = inputShape[2]

        imageProcessor = ImageProcessor.Builder()
//            .add(ResizeWithCropOrPadOp())
            .add(ResizeOp(modelInputWidth, modelInputHeight, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        inputTensorDataType = inputTensor.dataType()

        labels.clear()
        labels.addAll(FileUtil.loadLabels(context, LABEL_FILE))
    }

    private fun loadImage(inputTensorDataType: DataType, imageProcessor: ImageProcessor, bitmap: Bitmap): TensorImage {
        return imageProcessor.process(TensorImage(inputTensorDataType).apply {
            if (bitmap.config != Bitmap.Config.ARGB_8888) {
                // convert bitmap to ARGB888
                load(bitmap.copy(Bitmap.Config.ARGB_8888, true))
            } else {
                load(bitmap)
            }
        })
    }

    private fun argmax(map: Map<String, Float>): Pair<String, Float> {
        var maxKey = ""
        var maxValue = -1f

        map.entries.forEach { (key, value) ->
            if (value > maxValue) {
                maxValue = value
                maxKey = key
            }
        }

        return Pair(maxKey, maxValue)
    }

    fun release() {
        interpreter?.close()
        interpreter = null
    }
}