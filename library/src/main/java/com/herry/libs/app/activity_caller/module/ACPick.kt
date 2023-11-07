package com.herry.libs.app.activity_caller.module

import android.app.Activity
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.herry.libs.app.activity_caller.ACModule
import java.io.Serializable

class ACPick(private val caller: Caller, private val listener: OnListener): ACModule {

    interface OnListener: ACModule.OnPickerListener
    
    data class Result(
        val callActivity: Activity,
        val uris: MutableList<Uri> = mutableListOf(),
        val success: Boolean
    ) : Serializable
    
    open class Caller(
        internal val isMultiple: Boolean = false,
        internal val uri: Uri?,
        internal val onResult: ((result: Result) -> Unit)? = null
    )

    class PickImageOnly(
        isMultiple: Boolean = false,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(isMultiple, null, onResult)

    class PickVideoOnly(
        isMultiple: Boolean = false,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(isMultiple, null, onResult)

    class PickVisualMedia(
        isMultiple: Boolean = false,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(isMultiple, null, onResult)

    override fun call(activity: Activity) {
        when(caller) {
            is PickImageOnly -> {
                val isMultiple = caller.isMultiple
                listener.launchPicker(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) { uris ->
                    caller.onResult?.invoke(Result(activity, uris.toMutableList(), uris.isNotEmpty()))
                }
            }

            is PickVideoOnly -> {
                val isMultiple = caller.isMultiple
                listener.launchPicker(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) { uris ->
                    caller.onResult?.invoke(Result(activity, uris.toMutableList(), uris.isNotEmpty()))
                }
            }

            is PickVisualMedia -> {
                val isMultiple = caller.isMultiple
                listener.launchPicker(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) { uris ->
                    caller.onResult?.invoke(Result(activity, uris.toMutableList(), uris.isNotEmpty()))
                }
            }
        }
    }
}
