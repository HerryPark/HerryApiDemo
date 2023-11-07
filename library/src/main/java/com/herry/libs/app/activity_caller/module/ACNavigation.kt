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
import java.io.Serializable

open class ACNavigation(private val caller: Caller, private val listener: ACModule.OnIntentListener): ACModule {

    data class Result(
        val callActivity: Activity,
        val resultCode: Int,
        val intent: Intent?,
        val data: Bundle?
    ) : Serializable

    open class Caller(
        internal val transitionSharedElements: Array<Transition>? = null,
        internal val onResult: ((result: Result) -> Unit)? = null
    )

    class Transition(
        internal val view: View,
        internal val bitmap: Bitmap,
        internal val name: String
    )

    class IntentCaller (
        internal val intent: Intent,
        internal val bundle: Bundle? = null,
        transitions: Array<Transition>? = null,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(transitions, onResult)

    class NavCaller (
        internal val cls: Class<out ACActivity>,
        internal val bundle: Bundle? = null,
        internal val startDestination: Int = 0,
        internal val clearTop: Boolean = false,
        transitions: Array<Transition>? = null,
        onResult: ((result: Result) -> Unit)? = null
    ) : Caller(transitions, onResult)

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