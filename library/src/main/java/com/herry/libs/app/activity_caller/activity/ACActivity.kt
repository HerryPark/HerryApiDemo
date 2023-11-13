package com.herry.libs.app.activity_caller.activity

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.AC
import com.herry.libs.app.activity_caller.ACBase
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.result.ACActivityResultLaunchers
import com.herry.libs.app.activity_caller.result.TakeMediaRequest
import com.herry.libs.util.AppActivityManager

abstract class ACActivity : AppCompatActivity(), AC, AppActivityManager.OnGetAppActivityManager {

    lateinit var activityCaller: ACBase
        private set

    private lateinit var activityResultLaunchers: ACActivityResultLaunchers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLaunchers = ACActivityResultLaunchers(this)

        activityCaller = ACBase(callActivity = this, listener = object : ACBase.ACBaseListener {
            override fun launchIntent(intent: Intent, options: ActivityOptionsCompat?, onResult: ((result: ACNavigation.Result) -> Unit)?) {
                activityResultLaunchers.processLaunchActivity(intent, options, onResult)
            }

            override fun requestPermission(
                permission: Array<String>,
                showBlockedDefaultPopup: Boolean,
                onGranted: ((permission: Array<String>) -> Unit)?,
                onDenied: ((permission: Array<String>) -> Unit)?,
                onBlocked: ((permission: Array<String>) -> Unit)?,
                onCanceledBlockedPopup: ((dialog: DialogInterface) -> Unit)?
            ) {
                activityResultLaunchers.processRequestPermission(
                    permission,
                    onGranted = { permission1 ->
                        onGranted?.invoke(permission1)
                    },
                    onDenied = { permission1 ->
                        onDenied?.invoke(permission1)
                    },
                    onBlocked = { permission1 ->
                        if (showBlockedDefaultPopup) {
                            showBlockedPermissionPopup(permission1, onCanceledBlockedPopup)
                        }
                        onBlocked?.invoke(permission1)
                    })
            }

            override fun launchPicker(request: PickVisualMediaRequest, onResult: ((uris: List<Uri>) -> Unit)?) {
                activityResultLaunchers.processLaunchPicker(request, onResult)
            }

            override fun launchTake(request: TakeMediaRequest, onResult: ((success: Boolean) -> Unit)?) {
                activityResultLaunchers.processLaunchTake(request, onResult)
            }
        })
    }

    override fun onPause() {
        hideBlockedPermissionPopup()

        super.onPause()
    }

    override fun <T> call(caller: T?) {
        caller ?: return
        activityCaller.call(caller)
    }

    private var blockedPermissionPopup: Dialog? = null
    private fun showBlockedPermissionPopup(permissions: Array<String>, onCancel: ((dialog: DialogInterface) -> Unit)?) {
        hideBlockedPermissionPopup()
        blockedPermissionPopup = getBlockedPermissionPopup(permissions, onCancel)?.also {
            it.show()
        }
    }

    private fun hideBlockedPermissionPopup() {
        blockedPermissionPopup?.dismiss()
    }

    protected open fun getBlockedPermissionPopup(permissions: Array<String>, onCancel: ((dialog: DialogInterface) -> Unit)?): Dialog? = null

    override fun onDestroy() {
        activityResultLaunchers.unregisterAll()
        super.onDestroy()
    }

    override fun getAppActivityManager(): AppActivityManager? {
        return (application as? AppActivityManager.OnGetAppActivityManager)?.getAppActivityManager()
    }
}
