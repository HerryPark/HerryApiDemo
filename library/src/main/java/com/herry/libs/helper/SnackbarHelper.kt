package com.herry.libs.helper

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.GravityInt
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SnackbarHelper {
    enum class Length(val value: Int) {
        SHORT (Snackbar.LENGTH_SHORT),
        LONG (Snackbar.LENGTH_LONG),
        INDEFINITE (Snackbar.LENGTH_INDEFINITE)
    }

    // single instance
    private var snackbars: CopyOnWriteArrayList<Snackbar> = CopyOnWriteArrayList()

    /**
     * Show the snackbar with parameters
     *
     * @param activity - showing activity
     * @param message - message which is message resource
     * @param length - showing duration - Length.SHORT, Length.LONG, or Length.INDEFINITE }
     * @param horizontal - horizontal location on the activity
     * @param vertical - vertical location on the activity
     * @param marginVertical - vertical location's margin value
     */
    @MainThread
    fun show(
        activity: Activity?,
        @StringRes message: Int,
        length: Length = Length.SHORT,
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.BOTTOM,
        marginVertical: Int = -1,
        onMade: ((snackbar: Snackbar) -> Unit)? = null
    ) {
        show(
            activity = activity,
            message = activity?.getString(message),
            length = length,
            horizontal = horizontal,
            vertical = vertical,
            marginVertical = marginVertical,
            onMade = onMade
        )
    }

    /**
     * Show the snackbar with parameters
     *
     * @param activity - showing activity
     * @param message - message
     * @param length - showing duration - Length.SHORT, Length.LONG, or Length.INDEFINITE }
     * @param horizontal - horizontal location on the activity
     * @param vertical - vertical location on the activity
     * @param marginVertical - vertical location's margin value
     */
    @MainThread
    fun show(
        activity: Activity?,
        message: String?,
        length: Length = Length.SHORT,
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.BOTTOM,
        marginVertical: Int = -1,
        onMade: ((snackbar: Snackbar) -> Unit)? = null
    ) {
        val snackbar = make(
            activity = activity,
            message = message,
            length = length
        ) ?: return

        setShowSetting(
            snackbar,
            horizontal = horizontal,
            vertical = vertical,
            marginVertical = marginVertical)

        onMade?.invoke(snackbar)

        activity?.runOnUiThread {
            dismissAll()
            snackbar.show()
        }
    }

    /**
     * Show the snackbar with parameters
     *
     * @param parent - the view to find a parent from. This view is also used to find the anchor view when calling Snackbar.setAnchorView(int).
     * @param message - message
     * @param length - showing duration - Length.SHORT, Length.LONG, or Length.INDEFINITE }
     * @param horizontal - horizontal location on the activity
     * @param vertical - vertical location on the activity
     * @param marginVertical - vertical location's margin value
     */
    @MainThread
    fun show(
        parent: View?,
        @StringRes message: Int,
        length: Length = Length.SHORT,
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.BOTTOM,
        marginVertical: Int = -1,
        onMade: ((snackbar: Snackbar) -> Unit)? = null
    ) {
        show(
            parent = parent,
            message = parent?.context?.getString(message),
            length = length,
            horizontal = horizontal,
            vertical = vertical,
            marginVertical = marginVertical,
            onMade = onMade
        )
    }

    /**
     * Show the snackbar with parameters
     *
     * @param parent - the view to find a parent from. This view is also used to find the anchor view when calling Snackbar.setAnchorView(int).
     * @param message - message
     * @param length - showing duration - Length.SHORT, Length.LONG, or Length.INDEFINITE }
     * @param horizontal - horizontal location on the activity
     * @param vertical - vertical location on the activity
     * @param marginVertical - vertical location's margin value
     */
    @MainThread
    fun show(
        parent: View?,
        message: String?,
        length: Length = Length.SHORT,
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.BOTTOM,
        marginVertical: Int = -1,
        onMade: ((snackbar: Snackbar) -> Unit)? = null
    ) {
        val snackbar = make(
            rootView = parent,
            message = message,
            length = length
        ) ?: return

        setShowSetting(
            snackbar,
            horizontal = horizontal,
            vertical = vertical,
            marginVertical = marginVertical)

        onMade?.invoke(snackbar)

        parent?.post {
            dismissAll()
            snackbar.show()
        }
    }

    private fun setShowSetting(
        snackbar: Snackbar,
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.BOTTOM,
        marginVertical: Int = -1
    ) {
        snackbar.setLocation(
            horizontal = horizontal,
            vertical = vertical,
            marginVertical = marginVertical
        )

        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (transientBottomBar != null) {
                    this@SnackbarHelper.snackbars.remove(transientBottomBar)
                }
            }

            override fun onShown(sb: Snackbar?) {
                if (sb != null) {
                    this@SnackbarHelper.snackbars.add(sb)
                }
            }
        })
    }

    /**
     * Sets the snackbar's location on the view
     * @param horizontal - Gravity.START, Gravity.END, Gravity.LEFT, Gravity.RIGHT, or Gravity.CENTER_HORIZONTAL
     * @param vertical - Gravity.TOP, Gravity.BOTTOM, or Gravity.CENTER_VERTICAL
     * @param marginVertical - vertical margin
     */
    fun Snackbar.setLocation(
        @GravityInt horizontal: Int = Gravity.CENTER_HORIZONTAL,
        @GravityInt vertical: Int = Gravity.CENTER_VERTICAL,
        marginVertical: Int = -1
    ) {
        val snackBarView = this.view
        val params = snackBarView.layoutParams as? FrameLayout.LayoutParams
        if (params != null) {
            val location = horizontal or vertical
            params.gravity = location

            if (marginVertical >= 0) {
                when {
                    location and Gravity.TOP == Gravity.TOP -> {
                        params.topMargin = marginVertical
                    }

                    location and Gravity.BOTTOM == Gravity.BOTTOM -> {
                        params.bottomMargin = marginVertical
                    }

                    location and Gravity.CENTER_VERTICAL == Gravity.CENTER_VERTICAL -> {
                        params.topMargin = marginVertical / 2
                        params.bottomMargin = marginVertical / 2
                    }
                }
            }
            snackBarView.layoutParams = params
        }
    }

    @MainThread
    fun make(
        activity: Activity?,
        message: String?,
        length: Length = Length.SHORT
    ): Snackbar? {
        val rootView = activity?.findViewById<View?>(android.R.id.content) ?: return null

        return make(
            rootView = rootView,
            message = message,
            length = length
        )
    }

    @MainThread
    fun make(
        rootView: View?,
        message: String?,
        length: Length = Length.SHORT
    ): Snackbar? {
        rootView?.context ?: return null

        return Snackbar.make(rootView, message ?: "", length.value)
    }

    @MainThread
    fun dismissAll() {
        snackbars.forEach { previousSnackbar ->
            if (previousSnackbar.isShown) {
                previousSnackbar.dismiss()
            }
        }
    }
}