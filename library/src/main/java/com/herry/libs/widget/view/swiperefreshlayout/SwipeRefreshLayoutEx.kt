package com.herry.libs.widget.view.swiperefreshlayout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

@Suppress("unused")
class SwipeRefreshLayoutEx: SwipeRefreshLayout {
    private var isBlockRefreshingTouch = false

    private var mOnChildScrollUpListener: OnChildScrollUpListener? = null

    interface OnChildScrollUpListener {
        fun canChildScrollUp(): Boolean
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Listener that controls if scrolling up is allowed to child views or not
     */
    fun setOnChildScrollUpListener(listener: OnChildScrollUpListener?) {
        mOnChildScrollUpListener = listener
    }

    override fun canChildScrollUp(): Boolean {
        return mOnChildScrollUpListener?.canChildScrollUp() ?: super.canChildScrollUp()
    }

    fun setBlockRefreshingTouch(blockRefreshingTouch: Boolean) {
        isBlockRefreshingTouch = blockRefreshingTouch
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return isBlockRefreshingTouch && isRefreshing || super.dispatchTouchEvent(ev)
    }
}