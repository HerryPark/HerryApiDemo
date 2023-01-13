package com.herry.test.app.tflite.digitclassifier

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class DigitImageClassifier {

    companion object {
        // https://tfhub.dev/tensorflow/lite-model/efficientnet/lite4/fp32/2
        // TFLite (efficientnet/lite4/fp32) // "efficientnet_lite4_fp32_2.tflite"
        private const val MODEL_NAME = "keras_model_cnn.tflite"
    }

    data class Classified(
        val number: Int,
        val accuracy: Float
    )

    private var interpreter: Interpreter? = null
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0
    private var modelInputChannel: Int = 0
    private var modelOutputClasses: Int = 0

    @Suppress("SameParameterValue")
    @Throws(IOException::class)
    @WorkerThread
    private fun loadAssetModelFile(context: Context, modelName: String): ByteBuffer {
        val assetManager = context.assets

        val fd = assetManager.openFd(modelName)
        val fileInputStream = FileInputStream(fd.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = fd.startOffset
        val declaredLength = fd.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun initModelShape(interpreter: Interpreter) {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()

        modelInputChannel = inputShape[0]
        modelInputWidth = inputShape[1]
        modelInputHeight = inputShape[2]

        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()
        modelOutputClasses = outputShape[1]
    }

    // scale bitmap to image model size
    private fun resizeToModelSize(bitmap: Bitmap, toWidth: Int, toHeight: Int): Bitmap? {
        return if (toWidth > 0 && toHeight > 0
            && bitmap.width > 0 && bitmap.height >0) {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, toWidth, toHeight, false)
            resizedBitmap
        } else null
    }

    @WorkerThread
    private fun convertBitmapToGrayByteBuffer(bitmap: Bitmap): ByteBuffer? {
        if (bitmap.width < 0 || bitmap.height < 0) {
            return null
        }
        val byteBuffer = ByteBuffer.allocateDirect(bitmap.byteCount)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = pixel shr 16 and 0xFF
            val g = pixel shr 8 and 0xFF
            val b = pixel and 0xFF
            val avgPixelValue = (r + g + b) / 3.0f
            val normalizedPixelValue = avgPixelValue / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    @WorkerThread
    @Throws(Exception::class)
    fun classify(context: Context, image: Bitmap): Classified  {
        val interpreter = interpreter ?: (Interpreter(loadAssetModelFile(context, MODEL_NAME).apply {
            this.order(ByteOrder.nativeOrder())
        })).also {
            initModelShape(it)
            interpreter = it
        }

        val buffer = convertBitmapToGrayByteBuffer(resizeToModelSize(image, modelInputWidth, modelInputHeight) ?: throw NullPointerException())
        val result = Array(1) { FloatArray(modelOutputClasses) }
        interpreter.run(buffer, result)

        val classified = argmax(result[0])
        return Classified(classified.first, classified.second)
    }

    private fun argmax(array: FloatArray): Pair<Int, Float> {
        var argmax = 0
        var max = array[0]
        for (i in 1 until array.size) {
            val f = array[i]
            if (f > max) {
                argmax = i
                max = f
            }
        }
        return Pair(argmax, max)
    }

    fun release() {
        interpreter?.close()
        interpreter = null
    }
}