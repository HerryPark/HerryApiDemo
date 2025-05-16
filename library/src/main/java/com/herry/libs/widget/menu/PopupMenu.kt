package com.herry.libs.widget.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.PopupWindowCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.herry.libs.util.ViewUtil

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class PopupMenu(open val context: Context) {
    private var contentView: View? = null
    var window: PopupWindow? = null
        private set
    private var onDismissListener: PopupWindow.OnDismissListener? = null
    private val popupMenuOnDismissListener = PopupWindow.OnDismissListener { onDismiss() }
    private var isAutoHideOnPause: Boolean? = null
    private var fullScreenWidth: Boolean = false
    private var elevation: Float = 0f

    private var owner: LifecycleOwner? = null
    private val popupObserver by lazy { PopupObserver(this) }
    private var keepOnPause: Boolean = true

    fun setKeepOnPause(owner: LifecycleOwner, keep: Boolean) {
        this.owner = owner
        owner.lifecycle.addObserver(popupObserver)

        this.keepOnPause = keep
    }

    private class PopupObserver(val popupMenu: PopupMenu) : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            if (popupMenu.isShowing && !popupMenu.keepOnPause) {
                popupMenu.dismiss()
            }
            super.onPause(owner)
        }
    }

    interface OnPopupMenuItemSelectedListener {
        fun onPopupListMenuItemSelected(menu: PopupMenu?, id: Int)
    }

    interface OnListMenuAdapterItemClickListener {
        fun onClickItem(view: View?, position: Int)
    }


    private inner class PopupMenuLifecycleObserver : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_PAUSE) {
                if ( isAutoHideOnPause == true ) {
                    dismiss()
                }
            }
        }
    }

    fun setAutoHideOnPause(autoHideOnPause: Boolean) {
        if (context is LifecycleOwner) {
            if (isAutoHideOnPause == null) {
                val lifeCycle = (context as LifecycleOwner).lifecycle
                lifeCycle.addObserver(PopupMenuLifecycleObserver())
            }
            isAutoHideOnPause = autoHideOnPause
        }
    }

    fun setFullScreenWidth(fullScreenWidth: Boolean) {
        this.fullScreenWidth = fullScreenWidth
    }

    fun setElevation(elevation: Float) {
        this.elevation = elevation
    }

    protected abstract fun onContentView(context: Context): View?

    fun getContentView(context: Context): View? {
        return contentView ?: getContentView(context)
    }

    /**
     * Shows PopupMenu to anchor with gravity.
     *
     * @param anchor anchor view for popup menu
     * @param gravity Location of popup menu
     * (Gravity.RIGHT, Gravity.LEFT, Gravity.CENTER_HORIZONTAL
     * Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER_VERTICAL)
     * @param xOffset x offset from anchor
     * @param yOffset y offset from anchor
     */
    @JvmOverloads
    fun showFrom(anchor: View?, gravity: Int, xOffset: Int = 0, yOffset: Int = 0) {
        anchor ?: return

        val absGravity = Gravity.getAbsoluteGravity(gravity, anchor.layoutDirection)
        val hgrav = absGravity and Gravity.HORIZONTAL_GRAVITY_MASK
        val vgrav = absGravity and Gravity.VERTICAL_GRAVITY_MASK


        if (contentView == null) {
            contentView = onContentView(context)
        }

        val contentView = contentView ?: return

        // gets anchor location on screen
        val anchorLocation = IntArray(2)
        anchor.getLocationOnScreen(anchorLocation)

        // anchor size
        val anchorScreenX = anchorLocation[0]
        val anchorScreenY = anchorLocation[1]
        val anchorWidth = anchor.measuredWidth
        val anchorHeight = anchor.measuredHeight

        val screenWidth: Int
        val screenHeight: Int
        ViewUtil.getScreenSize(context).also {
            screenWidth = it.width
            screenHeight = it.height
        }

        // calculates content size
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED).also {
            if (fullScreenWidth || contentView.measuredWidth > screenWidth ) {
                val measureSpecWidth = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY)
                // recalculate with screen with
                contentView.measure(measureSpecWidth, View.MeasureSpec.UNSPECIFIED)
            }
        }

        val contentHeight = contentView.measuredHeight
        val contentWidth =  contentView.measuredWidth

        // gets gap with anchor and popup
        // base position of popup is (x, height) of anchor.
        var showXOffset = 0
        var showYOffset = 0
        when (hgrav) {
            Gravity.RIGHT -> {
                // sets popup x position to right of anchor
                showXOffset = anchorWidth + xOffset
            }
            Gravity.CENTER_HORIZONTAL -> {
                // sets popup x position to center horizontal of anchor
                showXOffset = (anchorWidth + xOffset) / 2
            }
            Gravity.LEFT -> {
                // sets popup x position to left of anchor
                showXOffset = -contentWidth - xOffset
            }
        }
        if (anchorScreenX + showXOffset + contentWidth > screenWidth - 1) {
            // if popup is located to out side of screen, fit popup to end of screen.
            showXOffset = screenWidth - 1 - contentWidth - anchorScreenX
        } else if (anchorScreenX + showXOffset < 0) {
            // if popup is located to out side of screen, fit popup to start of screen.
            showXOffset = 0
        }

        when (vgrav) {
            Gravity.TOP -> {
                // sets popup y position to above of anchor
                showYOffset = -anchorHeight - contentHeight - yOffset
            }
            Gravity.CENTER_VERTICAL -> {
                // sets popup y position to center vertical of anchor
                showYOffset = -(anchorHeight + yOffset) / 2
            }
            Gravity.BOTTOM -> {
                // sets popup y position to below of anchor
                showYOffset = yOffset
            }
        }
        if (anchorScreenY + anchorHeight + showYOffset + contentHeight > screenHeight - 1) {
            // if popup is located to out side of screen, fit popup to bottom of screen.
            showYOffset = screenHeight - 1 - contentHeight - anchorScreenY - anchorHeight
        } else if (anchorScreenY + showYOffset < 0) {
            // if popup is located to out side of screen, fit popup to top of screen.
            showYOffset = -anchorScreenY - anchorHeight
        }
        createWindow()

        // shows popup from anchor's (x, anchor height).
        PopupWindowCompat.showAsDropDown(window!!, anchor, showXOffset, showYOffset, Gravity.NO_GRAVITY)
    }

    private fun createWindow() {
        val cView = contentView ?: return

        PopupWindow(context).apply {
            this.setTouchInterceptor { view: View, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    releaseWindow()
                    return@setTouchInterceptor true
                } else if (event.action == MotionEvent.ACTION_BUTTON_PRESS) {
                    view.performClick()
                    return@setTouchInterceptor true
                }
                false
            }
            this.width = cView.measuredWidth
            this.height = cView.measuredHeight
            this.isFocusable = true
            this.isTouchable = true
            this.contentView = cView
            val popupMenuElevation = this@PopupMenu.elevation
            this.setBackgroundDrawable(if (popupMenuElevation > 0f) ColorDrawable(Color.WHITE) else ColorDrawable())
            this.setOnDismissListener(popupMenuOnDismissListener)
        }.also {
            window = it
        }
    }

    private fun releaseWindow() {
        window?.dismiss()
        window = null
    }

    fun setOnDismissListener(onDismissListener: PopupWindow.OnDismissListener?) {
        this.onDismissListener = onDismissListener
    }

    protected open fun onDismiss() {
        onDismissListener?.onDismiss()
        onDismissListener = null

        contentView = null
        releaseWindow()
    }

    open fun dismiss() {
        window?.dismiss()
    }

    open fun addItem(itemId: Int, @StringRes stringRsrc: Int, @DrawableRes iconRsrc: Int = 0, enabled: Boolean = true) {}

    open fun addItem(itemId: Int, label: String, icon: Drawable? = null, enabled: Boolean = true) {}

    open fun clear() {}

    open fun setOnPopupMenuItemSelectedListener(listener: OnPopupMenuItemSelectedListener?) {}

    val isShowing: Boolean
        get() = window?.isShowing ?: false
}
