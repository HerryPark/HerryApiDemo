package com.herry.libs.app.activity_caller.module

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.herry.libs.app.activity_caller.ACModule
import com.herry.libs.app.activity_caller.result.ACActivityResultContracts
import com.herry.libs.app.activity_caller.result.TakeMediaRequest
import com.herry.libs.log.Trace
import java.io.Serializable

class ACTake(private val caller: Caller, private val listener: OnListener) : ACModule {

    data class Result(
        val callActivity: Activity,
        val uri: Uri?,
        val success: Boolean
    ) : Serializable

    open class Caller(
        internal val uri: Uri,
        internal val onResult: ((result: Result) -> Unit)? = null
    )

    class TakeImage(
        uri: Uri,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(uri, onResult)

    class TakeVideo(
        uri: Uri,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(uri, onResult)

    interface OnListener: ACModule.OnTakeListener

    override fun call(activity: Activity) {
        when(caller) {
            is TakeImage -> {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    throw IllegalStateException("This device is need [${Manifest.permission.CAMERA}] permissions")
                }
                listener.launchTake(TakeMediaRequest.Builder()
                    .setMediaType(ACActivityResultContracts.TakeImage)
                    .setInputUri(caller.uri)
                    .build()) { success ->
                   caller.onResult?.invoke(Result(activity, if (success) caller.uri else null, success))
                }
            }

            is TakeVideo -> {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    throw IllegalStateException("This device is need [${Manifest.permission.CAMERA}] permissions")
                }
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    throw IllegalStateException("This device is need [${Manifest.permission.RECORD_AUDIO}] permissions")
                }
                listener.launchTake(TakeMediaRequest.Builder()
                    .setMediaType(ACActivityResultContracts.TakeVideo)
                    .setInputUri(caller.uri)
                    .build()) { success ->
                    caller.onResult?.invoke(Result(activity, if (success) caller.uri else null, success))
                }
            }
        }
    }
}