package com.herry.libs.widget.configure

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Suppress("MemberVisibilityCanBePrivate", "unused")
object SystemUI {

    fun isSystemNightMode(context: Context?): Boolean {
        return ((context?.resources?.configuration?.uiMode ?: 0) and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun setViewToFitSystemWindows(view: View?, isFit: Boolean) {
        view ?: return
        if (view.fitsSystemWindows != isFit) {
            view.fitsSystemWindows = isFit
        }
    }

    fun setDecorViewToFitSystemWindows(activity: Activity?, fit: Boolean) {
        setDecorViewToFitSystemWindows(activity?.window, fit)
    }

    fun setDecorViewToFitSystemWindows(window: Window?, fit: Boolean) {
        window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, fit)
    }

    /**
     * Gets the status bar background color form the applied theme @see style_dialog.xml
     */
    fun getSystemStatusBarBackgroundColor(context: Context?): Int {
        context ?: return 0
        val typedValue = TypedValue()
        val attrs: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.statusBarColor))
        val color = attrs.getColor(0, 0)
        attrs.recycle()

        return color
    }

    fun setStatusBar(activity: Activity?, appearance: SystemUIAppearance) {
        setStatusBar(
            activity = activity,
            backgroundColor = appearance.backgroundColor,
            appearanceColorStyle = appearance.appearanceColorStyle
        )
    }

    fun setStatusBar(
        activity: Activity?,
        @ColorInt backgroundColor: Int?,
        appearanceColorStyle: SystemUIAppearanceColorStyle? = null
    ) {
        val window = activity?.window ?: return

        val statusBarBackgroundColor = backgroundColor ?: window.statusBarColor
        val statusBarAppearanceColorStyle = when (appearanceColorStyle) {
            SystemUIAppearanceColorStyle.AUTO -> {
                // automatically check if the desired status bar is dark or light
                if (ColorUtils.calculateLuminance(statusBarBackgroundColor) > 0.5) SystemUIAppearanceColorStyle.LIGHT
                else SystemUIAppearanceColorStyle.DARK
            }
            else -> appearanceColorStyle
        }

        // sets the status bar appearance color (LIGHT or DARK))
        if (statusBarAppearanceColorStyle != null) {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = statusBarAppearanceColorStyle == SystemUIAppearanceColorStyle.LIGHT
        }
        // sets the status bar background color
        window.statusBarColor = statusBarBackgroundColor
    }

    fun getSystemNavigationBarBackgroundColor(context: Context?): Int {
        context ?: return 0
        val typedValue = TypedValue()
        val attrs: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.navigationBarColor))
        val color = attrs.getColor(0, 0)
        attrs.recycle()

        return color
    }

    fun setNavigationBar(activity: Activity?, appearance: SystemUIAppearance) {
        setNavigationBar(
            activity = activity,
            backgroundColor = appearance.backgroundColor,
            appearanceColorStyle = appearance.appearanceColorStyle
        )
    }

    fun setNavigationBar(activity: Activity?, @ColorInt backgroundColor: Int?, appearanceColorStyle: SystemUIAppearanceColorStyle? = null) {
        val window = activity?.window ?: return

        val navigationBarBackgroundColor = backgroundColor ?: window.navigationBarColor
        val navigationBarAppearanceColorStyle = when (appearanceColorStyle) {
            SystemUIAppearanceColorStyle.AUTO -> {
                // automatically check if the desired status bar is dark or light
                if (ColorUtils.calculateLuminance(navigationBarBackgroundColor) > 0.5) SystemUIAppearanceColorStyle.LIGHT
                else SystemUIAppearanceColorStyle.DARK
            }
            else -> appearanceColorStyle
        }

        // sets the navigation bar appearance color (LIGHT or DARK)
        if (navigationBarAppearanceColorStyle != null) {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = navigationBarAppearanceColorStyle == SystemUIAppearanceColorStyle.LIGHT
        }
        // sets the navigation bar background color
        window.navigationBarColor = navigationBarBackgroundColor
    }

    fun getSystemSoftInputMode(activity: Activity?): Int? {
        return activity?.window?.attributes?.softInputMode
    }

    fun isSystemFullScreen(activity: Activity?): Boolean {
        activity ?: return false
        val typedValue = TypedValue()
        val attrs: TypedArray = activity.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.windowFullscreen))
        val isFullScreen = attrs.getBoolean(0, false)
        attrs.recycle()
        return isFullScreen
    }

    fun isCurrentFullScreen(activity: Activity?): Boolean {
        val decorView = activity?.window?.decorView ?: return false
        return (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    fun isCurrentStatusBarVisible(activity: Activity?): Boolean {
        val decorView = activity?.window?.decorView ?: return false
        return ViewCompat.getRootWindowInsets(decorView)
            ?.isVisible(WindowInsetsCompat.Type.statusBars())
            ?: false
    }

    fun isCurrentNavigationBarVisible(activity: Activity?): Boolean {
        val decorView = activity?.window?.decorView ?: return false
        return ViewCompat.getRootWindowInsets(decorView)
            ?.isVisible(WindowInsetsCompat.Type.navigationBars())
            ?: false
    }

    fun setSystemUiVisibility(
        activity: Activity?,
        isFull: Boolean?,
        showBehavior: SystemUIShowBehavior? = null,
        statusBarVisibility: SystemUIVisibility? = null,
        navigationBarVisibility: SystemUIVisibility? = null,
        softInputMode: Int? = null
    ) {
        val window = activity?.window ?: return
        val decorView = window.decorView

        // sets full screen
        val isFullScreen = isFull ?: isCurrentFullScreen(activity)
        decorView.systemUiVisibility = if (isFullScreen) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            View.SYSTEM_UI_FLAG_VISIBLE
        }

        if (softInputMode != null) {
            window.setSoftInputMode(softInputMode)
        }

        val statusBarsType = WindowInsetsCompat.Type.statusBars()
        val navigationBarsType = WindowInsetsCompat.Type.navigationBars()

        var showTypes = 0
        var hideTypes = 0

        when (statusBarVisibility) {
            null -> {
                // keeps current setting
            }
            SystemUIVisibility.AUTO -> {
                // depends the fullscreen
                if (isFullScreen) hideTypes = hideTypes or statusBarsType
                else showTypes = showTypes or statusBarsType
            }
            SystemUIVisibility.SHOW -> showTypes = showTypes or statusBarsType
            SystemUIVisibility.HIDE -> hideTypes = hideTypes or statusBarsType
        }

        when (navigationBarVisibility) {
            null -> {
                // keeps current setting
            }
            SystemUIVisibility.AUTO,
            SystemUIVisibility.SHOW -> showTypes = showTypes or navigationBarsType
            SystemUIVisibility.HIDE -> hideTypes = hideTypes or navigationBarsType
        }

        // WindowInsetsController can hide or show specified system bars.
        val insetsController = WindowCompat.getInsetsController(window, decorView)
        if (statusBarVisibility != null) {
            insetsController.systemBarsBehavior = when (showBehavior) {
                SystemUIShowBehavior.DEFAULT -> WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
                SystemUIShowBehavior.TRANSIENT_BARS_BY_SWIPE -> WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                SystemUIShowBehavior.AUTO -> {
                    if (isFullScreen) WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    else WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
                }
                null -> insetsController.systemBarsBehavior
            }
        }

        if (showTypes != 0 || hideTypes !=0 ) {
            decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                if (showTypes != 0) {
                    insetsController.show(showTypes)
                }
                if (hideTypes != 0) {
                    insetsController.hide(hideTypes)
                }

                view.onApplyWindowInsets(windowInsets)
            }
        }
    }
}

data class SystemUIAppearances(
    val isFullScreen: Boolean?,
    val showBehavior: SystemUIShowBehavior?,
    val statusBar: SystemUIAppearance?,
    val navigationBar: SystemUIAppearance?,
    val softInputMode: Int?
) {
    companion object {
        fun getDefaultSystemUIAppearances(activity: Activity): SystemUIAppearances {
            return SystemUIAppearances(
                isFullScreen = SystemUI.isSystemFullScreen(activity),
                showBehavior = SystemUIShowBehavior.AUTO,
                statusBar = SystemUIAppearance.getDefaultStatusBarSystemUIAppearance(activity),
                navigationBar = SystemUIAppearance.getDefaultNavigationBarSystemUIAppearance(activity),
                softInputMode = SystemUI.getSystemSoftInputMode(activity)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SystemUIAppearances

        if (isFullScreen != other.isFullScreen) return false
        if (showBehavior != other.showBehavior) return false
        if (statusBar != other.statusBar) return false
        if (navigationBar != other.navigationBar) return false
        if (softInputMode != other.softInputMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isFullScreen?.hashCode() ?: 0
        result = 31 * result + (showBehavior?.hashCode() ?: 0)
        result = 31 * result + (statusBar?.hashCode() ?: 0)
        result = 31 * result + (navigationBar?.hashCode() ?: 0)
        result = 31 * result + (softInputMode ?: 0)
        return result
    }
}

data class SystemUIAppearance(
    val appearanceColorStyle: SystemUIAppearanceColorStyle?,
    @ColorInt val backgroundColor: Int?,
    val visibility: SystemUIVisibility?
) {
    companion object {
        fun getDefaultStatusBarSystemUIAppearance(context: Context): SystemUIAppearance {
            return SystemUIAppearance(
                appearanceColorStyle = SystemUIAppearanceColorStyle.AUTO,
                backgroundColor = SystemUI.getSystemStatusBarBackgroundColor(context),
                visibility = SystemUIVisibility.AUTO
            )
        }

        fun getDefaultNavigationBarSystemUIAppearance(context: Context): SystemUIAppearance {
            return SystemUIAppearance(
                appearanceColorStyle = SystemUIAppearanceColorStyle.AUTO,
                backgroundColor = SystemUI.getSystemNavigationBarBackgroundColor(context),
                visibility = SystemUIVisibility.AUTO
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SystemUIAppearance

        if (appearanceColorStyle != other.appearanceColorStyle) return false
        if (backgroundColor != other.backgroundColor) return false
        if (visibility != other.visibility) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appearanceColorStyle?.hashCode() ?: 0
        result = 31 * result + (backgroundColor ?: 0)
        result = 31 * result + (visibility?.hashCode() ?: 0)
        return result
    }


}

enum class SystemUIAppearanceColorStyle {
    AUTO,
    LIGHT,
    DARK
}

enum class SystemUIVisibility {
    SHOW,
    HIDE,
    AUTO // displays depending on full screen or not. if full screen is, it hides
}

enum class SystemUIShowBehavior {
    /**
     * https://developer.android.com/reference/androidx/core/view/WindowInsetsControllerCompat#BEHAVIOR_DEFAULT()
     */
    DEFAULT,

    /**
    https://developer.android.com/reference/androidx/core/view/WindowInsetsControllerCompat#BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE()
     */
    TRANSIENT_BARS_BY_SWIPE,

    // if the system ui is full screen, set to TRANSIENT_BARS_BY_SWIPE,
    // else set to DEFAULT
    AUTO
}
