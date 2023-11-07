package com.herry.test.app.tflite.liveclassifier

import android.graphics.Bitmap
import android.hardware.camera2.*
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.herry.libs.log.Trace
import com.herry.libs.permission.PermissionHelper
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import java.util.concurrent.Executors

class LiveClassifierPresenter : LiveClassifierContract.Presenter() {

//    private val executor = Executors.newSingleThreadExecutor()
//    private lateinit var bitmapBuffer: Bitmap
//
//    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
//    private val isFrontFacing get() = lensFacing == CameraSelector.LENS_FACING_FRONT
//
//    private var pauseAnalysis = false
//    private var imageRotationDegrees: Int = 0
//    private val tfImageBuffer = TensorImage(DataType.UINT8)
//
//    private val tfImageProcessor by lazy {
//        val cropSize = minOf(bitmapBuffer.width, bitmapBuffer.height)
//        ImageProcessor.Builder()
//            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
//            .add(
//                ResizeOp(
//                    tfInputSize.height, tfInputSize.width, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)
//            )
//            .add(Rot90Op(-imageRotationDegrees / 90))
//            .add(NormalizeOp(0f, 1f))
//            .build()
//    }
//
//    private val tfInputSize by lazy {
//        val inputIndex = 0
//        val inputShape = tflite.getInputTensor(inputIndex).shape()
//        Size(inputShape[2], inputShape[1]) // Order of axis is: {1, height, width, 3}
//    }

    override fun onResume(view: LiveClassifierContract.View, state: ResumeState) {
        // request camera and storage writing permission
//        view.onCheckPermission(PermissionHelper.Type.CAMERA, onGranted = {
//            loadCamera()
//        })
    }

//    private fun loadCamera() {
//        val context = view?.getViewContext() ?: return
//        val cameraPreviewView: PreviewView = view?.getCameraPreviewView() ?: return
//
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//        cameraProviderFuture.addListener ({
//
//            // Camera provider is now guaranteed to be available
//            val cameraProvider = cameraProviderFuture.get()
//
//            // Set up the view finder use case to display camera preview
//            val preview = Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(cameraPreviewView.display.rotation)
//                .build()
//
//            // Set up the image analysis use case which will process frames in real time
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation(cameraPreviewView.display.rotation)
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                .build()
//
//            var frameCounter = 0
//            var lastFpsTimestamp = System.currentTimeMillis()
//
//            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
//                if (!::bitmapBuffer.isInitialized) {
//                    // The image rotation and RGB image buffer are initialized only once
//                    // the analyzer has started running
//                    imageRotationDegrees = image.imageInfo.rotationDegrees
//                    bitmapBuffer = Bitmap.createBitmap(
//                        image.width, image.height, Bitmap.Config.ARGB_8888)
//                }
//
//                // Early exit: image analysis is in paused state
//                if (pauseAnalysis) {
//                    image.close()
//                    return@Analyzer
//                }
//
//                // Copy out RGB bits to our shared buffer
//                image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)  }
//
//                // Process the image in Tensorflow
//                val tfImage =  tfImageProcessor.process(tfImageBuffer.apply { load(bitmapBuffer) })
//
//                // Perform the object detection for the current frame
//                val predictions = detector.predict(tfImage)
//
//                // Report only the top prediction
//                reportPrediction(predictions.maxByOrNull { it.score })
//
//                // Compute the FPS of the entire pipeline
//                val frameCount = 10
//                if (++frameCounter % frameCount == 0) {
//                    frameCounter = 0
//                    val now = System.currentTimeMillis()
//                    val delta = now - lastFpsTimestamp
//                    val fps = 1000 * frameCount.toFloat() / delta
//                    Trace.d(TAG, "FPS: ${"%.02f".format(fps)} with tensorSize: ${tfImage.width} x ${tfImage.height}")
//                    lastFpsTimestamp = now
//                }
//            })
//
//            // Create a new camera selector each time, enforcing lens facing
//            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//
//            // Apply declared configs to CameraX using the same lifecycle owner
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, imageAnalysis)
//
//            // Use the camera object to link our preview use case with the view
//            preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)
//
//        }, ContextCompat.getMainExecutor(context))
//    }
}