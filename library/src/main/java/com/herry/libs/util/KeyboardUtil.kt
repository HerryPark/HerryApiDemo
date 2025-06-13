package com.herry.libs.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.ResultReceiver
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

@Suppress("unused")
object KeyboardUtil {
    fun hideSoftKeyboard(activity: Activity?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val focusedView = activity?.currentFocus
        if (focusedView != null) {
            hideSoftKeyboard(focusedView = focusedView, flag = flag, resultReceiver = resultReceiver)
        } else {
            val window = activity?.window ?: return
            WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
        }
    }

    fun hideSoftKeyboard(fragment: Fragment?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val focusedView = fragment?.view?.findFocus()
            ?: (fragment as? DialogFragment)?.dialog?.window?.decorView
            ?: fragment?.activity?.currentFocus

        if (focusedView != null) {
            hideSoftKeyboard(focusedView = focusedView, flag = flag, resultReceiver = resultReceiver)
        } else {
            hideSoftKeyboard(activity = fragment?.activity, flag = flag, resultReceiver = resultReceiver)
        }
    }

    fun hideSoftKeyboard(focusedView: View?, flag: Int = 0, resultReceiver: ResultReceiver? = null) {
        val context = focusedView?.context ?: return

        focusedView.clearFocus()
        val imm = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focusedView.windowToken, flag, resultReceiver)
    }

    fun showSoftKeyboard(focusView: View?, resultReceiver: ResultReceiver? = null) {
        val context = focusView?.context ?: return

        focusView.postDelayed({
            focusView.isFocusable = true
            focusView.isFocusableInTouchMode = true
            focusView.requestFocus()
            val imm = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(focusView, 0, resultReceiver)
        }, context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
    }

    fun isSoftKeyboardShown(activity: Activity?): Boolean {
        val window = activity?.window ?: return false
        return ViewCompat.getRootWindowInsets(window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true
    }

    fun isSoftKeyboardShown(fragment: Fragment?): Boolean = isSoftKeyboardShown(fragment?.activity)

    fun isConnectedHardwareKeyboard(activity: Activity?): Boolean {
        activity ?: return false
        val configuration = activity.resources.configuration
        return configuration.navigation == Configuration.NAVIGATION_DPAD
                || configuration.navigation == Configuration.NAVIGATION_TRACKBALL
                || configuration.navigation == Configuration.NAVIGATION_WHEEL
    }
}
