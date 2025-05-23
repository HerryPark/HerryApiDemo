package com.herry.libs.app.activity_caller.module

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.herry.libs.app.activity_caller.ACModule
import com.herry.libs.app.activity_caller.activity.ACActivity
import com.herry.libs.app.nav.NavMovement
import com.herry.libs.util.AppActivityManager
import java.io.Serializable

open class ACNavigation(private val caller: Caller, private val listener: ACModule.OnIntentListener): ACModule {
    enum class Error {
        EXIST_ACTIVITY
    }

    data class Result(
        val callActivity: Activity,
        val resultCode: Int,
        val intent: Intent?,
        val data: Bundle?,
        val error: Error? = null
    ) : Serializable

    open class Caller(
        internal val transitionSharedElements: Array<Transition>? = null,
        internal val onResult: ((result: Result) -> Unit)? = null,
        internal val allowDuplicated: Boolean = true
    )

    class Transition(
        internal val view: View,
        internal val bitmap: Bitmap,
        internal val name: String
    )

    class IntentCaller (
        internal val intent: Intent,
        internal val bundle: Bundle? = null,
        allowDuplicated: Boolean = true,
        transitions: Array<Transition>? = null,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(transitions, onResult, allowDuplicated)

    class NavCaller (
        internal val cls: Class<out ACActivity>,
        internal val bundle: Bundle? = null,
        internal val startDestination: Int = 0,
        internal val clearTop: Boolean = false,
        allowDuplicated: Boolean = true,
        transitions: Array<Transition>? = null,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(transitions, onResult, allowDuplicated)

    protected open fun getCallerIntent(activity: Activity): Intent? {
        return when(caller) {
            is NavCaller -> {
                Intent(activity, caller.cls).apply {
                    if (caller.clearTop) {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    caller.bundle?.let {
                        putExtra(NavMovement.NAV_BUNDLE, it)
                    }
                    if (caller.startDestination != 0) {
                        putExtra(NavMovement.NAV_START_DESTINATION, caller.startDestination)
                    }
                }
            }
            is IntentCaller -> {
                caller.intent.apply {
                    caller.bundle?.let {
                        putExtra(NavMovement.NAV_BUNDLE, it)
                    }
                }
            }
            else -> null
        }
    }

    override fun call(activity: Activity) {
        val intent = getCallerIntent(activity) ?: return

        val onResult = caller.onResult

        if (!caller.allowDuplicated && activity is AppActivityManager.OnGetAppActivityManager) {
            val targetClass = try {
                Class.forName(intent.component?.className ?: throw ClassNotFoundException()) ?: return
            } catch (_: Exception) {
                return
            }
            if (activity.getAppActivityManager()?.getActivity(targetClass) != null) {
                onResult?.invoke(Result(
                    callActivity = activity,
                    resultCode = Activity.RESULT_CANCELED,
                    intent = null,
                    data = null,
                    error = Error.EXIST_ACTIVITY
                ))
                return
            }
        }
        if (caller.transitionSharedElements != null) {
            var options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
            if (caller.transitionSharedElements.isNotEmpty()) {
                val pairs = mutableListOf<androidx.core.util.Pair<View, String>>()
                val bitmaps = mutableListOf<Pair<String, Bitmap>>()

                for (transition in caller.transitionSharedElements) {
                    pairs.add(androidx.core.util.Pair.create(transition.view, transition.name))
                    bitmaps.add(Pair(transition.name, transition.bitmap))
                }

                if (pairs.isNotEmpty() && bitmaps.isNotEmpty()) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        *pairs.toTypedArray()
                    )
                    for (bitmap in bitmaps) {
                        ACTransitionUtil.BitmapStorage.put(bitmap.first, bitmap.second)
                    }
                }

                if (onResult != null) {
                    listener.launchIntent(intent, options, onResult)
                } else {
                    activity.startActivity(intent, options.toBundle())
                }
            }
        } else {
            if (onResult != null) {
                listener.launchIntent(intent, null, onResult)
            } else {
                activity.startActivity(intent)
            }
        }
    }
}