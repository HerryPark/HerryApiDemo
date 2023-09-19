package com.herry.libs.widget.view.viewgroup

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

class LoadingCountView : FrameLayout {

    private enum class Status {
        INIT,
        LOAD_WAIT,
        LOADING,
        END_WAIT,
        ENDING,
    }

    private var status = Status.INIT
    private var loadingCount = 0
    private var loadingProgressBar: ProgressBar? = null

    private var waitToIng: WaitToIng? = null

    companion object {
        private const val  SHOW_LOADING_DELAY = 300L
        private const val  HIDE_LOADING_DELAY = 0L
    }

    private inner class WaitToIng(val waitStatus: Status): Runnable {
        private var cancel: Boolean = false

        fun cancel() {
            cancel = true
        }

        fun execute(): WaitToIng {
            when(waitStatus) {
                Status.LOAD_WAIT -> postDelayed(this, SHOW_LOADING_DELAY)
                Status.END_WAIT -> postDelayed(this, HIDE_LOADING_DELAY)
                else -> {

                }
            }
            return this
        }

        override fun run() {
            if(cancel) {
                return
            }

            when(waitStatus) {
                Status.LOAD_WAIT -> statusToLoading()
                Status.END_WAIT -> statusToEnding()
                else -> {

                }
            }
        }

    }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0): super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        loadingProgressBar = ProgressBar(context)
        loadingProgressBar?.run {
            this@LoadingCountView.addView(this, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                this.gravity = Gravity.CENTER
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean = true

    fun show() {
        when(status) {
            Status.INIT -> {
                status = Status.LOAD_WAIT
                loadingCount = 1
                waitToIng?.cancel()
                waitToIng = WaitToIng(status).execute()
            }
            Status.LOAD_WAIT, Status.LOADING  -> loadingCount++
            Status.END_WAIT -> {
                waitToIng?.cancel()
                waitToIng = null
                status = Status.LOADING
                loadingCount = 1
            }
            Status.ENDING -> {
                status = Status.LOAD_WAIT
                loadingCount = 1
                statusToLoading()
            }
        }
    }

    fun hide(force: Boolean = false, listener: OnHideListener? = null) {
        this.onHideListener = listener

        when(status) {
            Status.INIT -> {
            }
            Status.LOAD_WAIT -> {
                loadingCount--
                if(loadingCount <= 0) {
                    status = Status.INIT
                }
            }
            Status.LOADING -> {
                loadingCount--
                if(loadingCount <= 0) {
                    status = Status.END_WAIT
                    waitToIng?.cancel()
                    waitToIng = WaitToIng(status).execute()
                }
            }
            Status.END_WAIT -> {
                loadingCount = 0
            }
            Status.ENDING -> {
            }

        }
    }

    private fun statusToLoading() {
        if(status == Status.LOAD_WAIT) {
            status = Status.LOADING
            animate()
                .setListener(null)
                .cancel()
            alpha = 1.0f
            visibility = View.VISIBLE
        }
    }

    private fun statusToEnding() {
        if(status == Status.END_WAIT) {
            status = Status.ENDING
            animate()
                .setListener(null)
                .cancel()

            alpha = 0.0f
            statusToInit()
            onHideListener?.onDone()
        }
    }

    private fun statusToInit() {
        status = Status.INIT
        loadingCount = 0

        animate()
            .setListener(null)
            .cancel()

        visibility = View.GONE
    }

    interface OnHideListener {
        fun onDone()
    }

    private var onHideListener: OnHideListener? = null
}