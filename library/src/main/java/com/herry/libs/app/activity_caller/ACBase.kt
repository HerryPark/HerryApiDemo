package com.herry.libs.app.activity_caller

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.module.ACError
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.app.activity_caller.module.ACPick
import com.herry.libs.app.activity_caller.module.ACTake
import com.herry.libs.app.activity_caller.result.TakeMediaRequest

class ACBase(private val callActivity: Activity, private val listener: ACBaseListener) : AC {
    interface ACBaseListener : ACModule.OnIntentListener, ACModule.OnPermissionListener, ACModule.OnPickerListener, ACModule.OnTakeListener

    override fun <T> call(caller: T?) {
        caller ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callOnMainLooper(caller)
        } else {
            Handler(Looper.getMainLooper()).post {
                callOnMainLooper(caller)
            }
        }
    }

    private fun <T> callOnMainLooper(caller: T) {
        when (caller) {
            is ACError.Caller -> {
                ACError(caller, object : ACError.ACErrorListener {
                }).also { it.call(activity = callActivity) }
            }
            is ACPermission.Caller -> {
                ACPermission(caller, object : ACPermission.OnListener {
                    override fun requestPermission(
                        permission: Array<String>,
                        showBlockedDefaultPopup: Boolean,
                        onGranted: ((permission: Array<String>) -> Unit)?,
                        onDenied: ((permission: Array<String>) -> Unit)?,
                        onBlocked: ((permission: Array<String>) -> Unit)?,
                        onCanceledBlockedPopup: ((dialog: DialogInterface) -> Unit)?
                    ) = listener.requestPermission(permission, showBlockedDefaultPopup, onGranted, onDenied, onBlocked, onCanceledBlockedPopup)
                }).also { it.call(activity = callActivity) }
            }
            is ACTake.Caller -> {
                ACTake(caller, object : ACTake.OnListener {
                    override fun launchTake(request: TakeMediaRequest, onResult: ((success: Boolean) -> Unit)?) {
                        listener.launchTake(request, onResult)
                    }
                }).also { it.call(activity = callActivity) }
            }
            is ACPick.Caller -> {
                ACPick(caller, object : ACPick.OnListener {
                    override fun launchPicker(request: PickVisualMediaRequest, onResult: ((uris: List<Uri>) -> Unit)?) {
                        listener.launchPicker(request, onResult)
                    }
                }).also { it.call(activity = callActivity) }
            }
            is ACNavigation.Caller -> {
                ACNavigation(caller, object : ACModule.OnIntentListener {
                    override fun launchIntent(intent: Intent, options: ActivityOptionsCompat?, onResult: ((result: ACNavigation.Result) -> Unit)?) {
                        listener.launchIntent(intent, options, onResult)
                    }
                }).also { it.call(activity = callActivity) }
            }
        }
    }
}