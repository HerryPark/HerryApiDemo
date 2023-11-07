package com.herry.libs.app.activity_caller.module

import android.app.Activity
import android.content.DialogInterface
import com.herry.libs.app.activity_caller.ACModule

class ACPermission(private val caller: Caller, private val listener: OnListener): ACModule {

    class Caller(
        internal val permissions: Array<String>,
        internal val showBlockedDefaultPopup: Boolean = false,
        internal val onGranted: ((permission: Array<String>) -> Unit)? = null,
        internal val onDenied: ((permission: Array<String>) -> Unit)?  = null,
        internal val onBlocked: ((permission: Array<String>) -> Unit)?  = null,
        internal val onCanceledBlockedPopup: ((dialog: DialogInterface) -> Unit)? = null
    )

    interface OnListener: ACModule.OnIntentListener, ACModule.OnPermissionListener

    override fun call(activity: Activity) {
        listener.requestPermission(
            caller.permissions,
            caller.showBlockedDefaultPopup,
            caller.onGranted,
            caller.onDenied,
            caller.onBlocked,
            caller.onCanceledBlockedPopup
        )
    }
}