package com.herry.libs.widget.recyclerview.snap.loop

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
class AutoLoopPagerSnapExHelper : LoopPagerSnapExHelper() {
    // auto scrolling loop pager snap
    private var period: Long = 0

    private val timerLock = Any()

    companion object {
        private const val MSG_NEXT = 1
    }

    @SuppressLint("HandlerLeak")
    private val timerHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_NEXT -> {
                    val recyclerView: RecyclerView? = getRecyclerView()
                    if (null != recyclerView
                        && recyclerView.isAttachedToWindow
                        && recyclerView.isShown
                        && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
                    ) {
                        snapToNext()
                    }
                    sendMessageNext(period)
                }
            }
        }
    }

    private fun sendMessageNext(period: Long) {
        synchronized(timerLock) {
            timerHandler.removeCallbacksAndMessages(null)
            if (0 < period) {
                val message = Message()
                message.what = MSG_NEXT
                timerHandler.sendMessageDelayed(message, period)
            }
        }
    }

    private fun enableAuto(enable: Boolean) {
        getRecyclerView() ?: return

        stopTimer()

        if (enable) {
            sendMessageNext(period)
        }
    }

    fun setPeriod(period: Long) {
        this.period = period
    }

    fun startAuto(period: Long) {
        enableAuto(false)

        this.period = period
        if (0 < this.period) {
            enableAuto(true)
        }
    }

    fun startAuto() {
        if (0 < period) {
            enableAuto(true)
        }
    }

    fun stopAuto() {
        enableAuto(false)
    }

    private fun stopTimer() {
        synchronized(timerLock) { timerHandler.removeCallbacksAndMessages(null) }
    }

}