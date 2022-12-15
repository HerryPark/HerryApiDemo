package com.herry.libs.app.activity_caller.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.AC
import com.herry.libs.app.activity_caller.ACBase
import com.herry.libs.app.activity_caller.module.ACNavigation

abstract class ACActivity : AppCompatActivity(), AC {

    lateinit var activityCaller: ACBase
        private set

    private lateinit var activityResultLaunchers: ActivityResultLaunchers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLaunchers = ActivityResultLaunchers(this)

        activityCaller = ACBase(object : ACBase.ACBaseListener {
            override fun getActivity(): ComponentActivity = this@ACActivity

            override fun launchActivity(intent: Intent, options: ActivityOptionsCompat?, onResult: ((result: ACNavigation.Result) -> Unit)?) {
                activityResultLaunchers.processLaunchActivity(intent, options, onResult)
            }

            override fun requestPermission(permission: Array<String>, onGranted: ((permission: Array<String>) -> Unit)?, onDenied: ((permission: Array<String>) -> Unit)?, onBlocked: ((permission: Array<String>) -> Unit)?) {
                activityResultLaunchers.processRequestPermission(
                    permission,
                    onGranted = { _permission ->
                        onGranted?.let { it(_permission) }
                    },
                    onDenied = { _permission ->
                        onDenied?.let { it(_permission) }
                    },
                    onBlocked = { _permission ->
                        if (onBlocked == null) {
                            showBlockedPermissionPopup(_permission)
                        } else {
                            onBlocked(_permission)
                        }
                    })
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
    private fun showBlockedPermissionPopup(permissions: Array<String>) {
        hideBlockedPermissionPopup()
        blockedPermissionPopup = getBlockedPermissionPopup(permissions)?.also {
            it.show()
        }
    }

    private fun hideBlockedPermissionPopup() {
        blockedPermissionPopup?.dismiss()
    }

    protected open fun getBlockedPermissionPopup(permissions: Array<String>): Dialog? = null

    override fun onDestroy() {
        activityResultLaunchers.unregisterAll()
        super.onDestroy()
    }
}
