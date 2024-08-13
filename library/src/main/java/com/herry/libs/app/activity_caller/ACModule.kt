package com.herry.libs.app.activity_caller

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.result.TakeMediaRequest
import kotlin.jvm.Throws

interface ACModule {
    interface OnIntentListener {
        @Throws(ActivityNotFoundException::class)
        fun launchIntent(
            intent: Intent,
            options: ActivityOptionsCompat?,
            onResult: ((result: ACNavigation.Result) -> Unit)? = null
        ) {}
    }

    interface OnPermissionListener {
        fun requestPermission(
            permission: Array<String>,
            showBlockedDefaultPopup: Boolean = false,
            onGranted: ((permission: Array<String>) -> Unit)?,
            onDenied: ((permission: Array<String>) -> Unit)?,
            onBlocked: ((permission: Array<String>) -> Unit)?,
            onCanceledBlockedPopup: ((dialog: DialogInterface) -> Unit)? = null
        )
    }

    interface OnPickerListener {
        @Throws(ActivityNotFoundException::class)
        fun launchPicker(
            request: PickVisualMediaRequest,
            onResult: ((uris: List<Uri>) -> Unit)?
        )
    }

    interface OnTakeListener {
        @Throws(ActivityNotFoundException::class)
        fun launchTake(
            request: TakeMediaRequest,
            onResult: ((success: Boolean) -> Unit)?
        )
    }

    fun call(activity: Activity)
}