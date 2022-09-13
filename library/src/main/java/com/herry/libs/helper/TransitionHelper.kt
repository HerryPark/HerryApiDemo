package com.herry.libs.helper

import android.app.Activity
import android.transition.Transition
import android.transition.TransitionInflater
import androidx.annotation.TransitionRes
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class TransitionHelper(
    @TransitionRes private val enterTransition: Int = 0,
    @TransitionRes private val exitTransition: Int = 0,
    private val listener: WeakReference<TransitionHelperListener>
) {

    interface TransitionHelperListener {
        fun onTransitionStart()
        fun onTransitionEnd()
    }

    private var transitionCount = 0

    fun isTransition(): Boolean {
        return transitionCount > 0
    }

    private val transitionListener = object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
            transitionCount++
            if (transitionCount <= 1) {
                transitionCount = 1
                listener.get()?.onTransitionStart()
            }
        }

        override fun onTransitionEnd(transition: Transition) {
            transitionCount--
            if (transitionCount <= 0) {
                transitionCount = 0
                listener.get()?.onTransitionEnd()
            }
        }

        override fun onTransitionCancel(transition: Transition) {}

        override fun onTransitionPause(transition: Transition) {}

        override fun onTransitionResume(transition: Transition) {}
    }

    fun onCreate(activity: Activity?, fragment: Fragment?) {
        activity?.let {
            it.window?.enterTransition?.addListener(transitionListener)
            it.window?.sharedElementEnterTransition?.addListener(transitionListener)

            val enterTransition =
                if (enterTransition != 0) TransitionInflater.from(it).inflateTransition(enterTransition) else null
            val exitTransition =
                if (exitTransition != 0) TransitionInflater.from(it).inflateTransition(exitTransition) else null
            enterTransition?.let { _it ->
                _it.addListener(transitionListener)
                fragment?.enterTransition = _it
            }
            exitTransition?.let { _it ->
                fragment?.exitTransition = _it
            }
            fragment?.allowEnterTransitionOverlap = true
        }
    }

    fun onDestroy(activity: Activity?) {
        activity?.let {
            it.window?.enterTransition?.removeListener(transitionListener)
            it.window?.sharedElementEnterTransition?.removeListener(transitionListener)
        }
    }
}