package com.herry.libs.app.activity_caller.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import com.herry.libs.log.Trace

object ACActivityResultContracts {
    class TakeMedia : ActivityResultContract<TakeMediaRequest, Boolean>() {
        @CallSuper
        override fun createIntent(context: Context, input: TakeMediaRequest): Intent {
            val uri: Uri?
            return (when (val mediaType = input.mediaType) {
                is TakeImage -> {
                    uri = input.input ?: throw IllegalArgumentException("uri is null for the $mediaType")
                    ActivityResultContracts.TakePicture().createIntent(context, uri)
                }
                is TakeVideo -> {
                    uri = input.input ?: throw IllegalArgumentException("uri is null for the $mediaType")
                    ActivityResultContracts.CaptureVideo().createIntent(context, uri)
                }
            })
        }

        override fun getSynchronousResult(
            context: Context,
            input: TakeMediaRequest
        ): SynchronousResult<Boolean>? = null

        @Suppress("AutoBoxing")
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }

    /**
     * Request type for the TakeCameraRequest
     */
    sealed interface TakeMediaType

    data object TakeImage: TakeMediaType

    data object TakeVideo: TakeMediaType
}