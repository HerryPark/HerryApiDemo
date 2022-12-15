package com.herry.libs.app.activity_caller

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.module.ACError
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.app.activity_caller.module.ACTake

class ACBase(private val listener: ACBaseListener) : AC {
    interface ACBaseListener : ACModule.OnPermissionListener {
        fun getActivity(): ComponentActivity

        fun launchActivity(intent: Intent,
                           options: ActivityOptionsCompat?,
                           onResult: ((result: ACNavigation.Result) -> Unit)?
        )
    }

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
                val module = ACError(caller, object : ACError.ACErrorListener {
                    override fun getActivity(): ComponentActivity = listener.getActivity()
                })
                module.call()
            }
            is ACPermission.Caller -> {
                val module = ACPermission(caller, object : ACPermission.ACPermissionListener {
                    override fun getActivity(): ComponentActivity = listener.getActivity()

                    override fun requestPermission(
                        permission: Array<String>,
                        onGranted: ((permission: Array<String>) -> Unit)?,
                        onDenied: ((permission: Array<String>) -> Unit)?,
                        onBlocked: ((permission: Array<String>) -> Unit)?
                    ) = listener.requestPermission(permission, onGranted, onDenied, onBlocked)
                })
                module.call()
            }
            is ACTake.Caller -> {
                val module = ACTake(caller, object : ACTake.ACTakeListener {
                    override fun getActivity(): ComponentActivity = listener.getActivity()
                })
                module.call()
            }
            is ACNavigation.Caller -> {
                val module = ACNavigation(caller, object : ACModule.OnListener<ACNavigation> {

                    override fun getActivity(): ComponentActivity = listener.getActivity()

                    override fun launchActivity(intent: Intent, options: ActivityOptionsCompat?, onResult: ((result: ACNavigation.Result) -> Unit)?) {
                        listener.launchActivity(intent, options, onResult)
                    }
                })
                module.call()
            }
        }
    }
}