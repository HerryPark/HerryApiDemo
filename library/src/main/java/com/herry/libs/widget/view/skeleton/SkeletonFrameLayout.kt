package com.herry.libs.widget.view.skeleton

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.core.view.isVisible
import com.herry.libs.R
import com.herry.libs.widget.anim.ViewAnimCreator
import com.herry.libs.widget.anim.ViewAnimListener
import com.herry.libs.widget.anim.ViewAnimPlayer


class SkeletonFrameLayout : FrameLayout {
    private enum class State {
        STARTED,
        PAUSED,
        STOPPED
    }

    private var isAutoStart = false
    private var effectDuration = 1000
    private var state: State = State.STOPPED
    private var effectAnimPlayer: ViewAnimPlayer? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.SkeletonFrameLayout)
        isAutoStart = attr.getBoolean(R.styleable.SkeletonFrameLayout_sf_auto_start, false)
        effectDuration = attr.getInteger(R.styleable.SkeletonFrameLayout_sf_effect_duration, 1000)

        attr.recycle()
    }

    private fun isStarted(): Boolean = state == State.STARTED || state == State.PAUSED

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (changedView == this) {
            if (visibility == View.VISIBLE) {
                if (state == State.PAUSED) {
                    startEffect()
                }
            } else {
                if (state == State.STARTED) {
                    state = State.PAUSED
                }
                stopEffect()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isAutoStart) {
            startEffect()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        stopEffect()
    }

    private fun getChildViews(viewGroup: ViewGroup): MutableList<View> {
        val views = mutableListOf<View>()

        viewGroup.children.forEach { child ->
            if (child is ViewGroup) {
                views.addAll(getChildViews(child))
            } else {
                views.add(child)
            }
        }

        return views
    }

    fun setEffectAnimPlayer(animPlayer: ViewAnimPlayer?) {
        this.effectAnimPlayer = animPlayer
    }

    fun startEffect() {
        val effectAnimPlayer = this.effectAnimPlayer ?: ViewAnimPlayer().apply {
            val list = getChildViews(this@SkeletonFrameLayout)

            add(ViewAnimCreator(*list.toTypedArray()).apply {
                duration(effectDuration.toLong())
                alpha(0.5f, 1f)
            })
            setRepeatCount(ViewAnimPlayer.INFINITE)
            setRepeatMode(ObjectAnimator.REVERSE)
        }

        if (isStarted()) {
            return
        }

        state = State.STARTED
        effectAnimPlayer.start()
    }

    fun stopEffect() {
        state = State.STOPPED
        effectAnimPlayer?.cancel()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setProtectTouchBelowLayer(protect: Boolean) {
        setOnTouchListener { _: View?, _: MotionEvent? -> protect }
    }

    private var hidingAnimPlayer: ViewAnimPlayer? = null
    fun hide(withAnimation: Boolean = true, duration: Long = 500L) {
        if (!withAnimation) {
            this.isVisible = false
            return
        }

        if (!this.isVisible || hidingAnimPlayer != null) {
            return
        }
        val view = this@SkeletonFrameLayout
        hidingAnimPlayer = ViewAnimPlayer().apply {
            add(
                ViewAnimCreator(view).apply {
                    alpha(1f, 0f)
                }
                    .duration(duration)
            )

            this.onStartListener = object: ViewAnimListener.OnStart {
                override fun onStart() {
                    view.stopEffect()
                }
            }
            this.onStopListener = object: ViewAnimListener.OnStop {
                override fun onStop() {
                    view.isVisible = false
                }
            }
        }
        hidingAnimPlayer?.start()
    }

    fun show() {
        hidingAnimPlayer?.cancel()
        this.isVisible = true
    }
}