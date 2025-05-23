package com.herry.libs.widget.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.herry.libs.widget.anim.ViewAnimListener.*
import java.util.*

@Suppress("unused")
class ViewAnimPlayer {
    private val animations: MutableList<ViewAnimCreator> = ArrayList()
    private var animatorSet: AnimatorSet? = null
    private var prev: ViewAnimPlayer? = null
    private var next: ViewAnimPlayer? = null
    var onStartListener: OnStart? = null
    var onStopListener: OnStop? = null
    var onCancelListener: OnCancel? = null
    private var repeatCount = 0
    private var repeatMode = ObjectAnimator.RESTART

    fun add(vararg viewAnim: ViewAnimCreator) {
        if (animations.isEmpty()) {
            animations.addAll(listOf(*viewAnim))
        } else {
            if (null == next) {
                next = ViewAnimPlayer()
                next?.prev = this
            }
            next?.add(*viewAnim)
        }
    }

    fun start(delay: Long = 0) {
        animatorSet?.cancel()
        animatorSet = null

        animatorSet = createAnimatorSet()
        animatorSet?.run {
            for (animator in this.childAnimations) {
                if (animator !is ObjectAnimator) {
                    continue
                }
                animator.repeatCount = if (0 > repeatCount) ObjectAnimator.INFINITE else repeatCount
                animator.repeatMode = repeatMode
            }
            if (delay > 0) {
                startDelay = delay
            } else {
                startDelay = 0
            }
            this.start()
        }
    }


    fun cancel() {
        animatorSet?.cancel()
        animatorSet = null

        next?.cancel()
    }

    private fun createAnimatorSet(): AnimatorSet? {
        var duration: Long = 0
        //        Interpolator interpolator = null;
        var startDelay: Long = 0
        val playerAnimators: MutableList<Animator> = ArrayList()
        val isResetAnimation = animations.firstOrNull { !it.keepEnd } != null

        for (viewAnim in animations) {
            viewAnim.keepEnd
            val animators = viewAnim.getAnimators()
            playerAnimators.addAll(animators)
            if (duration < viewAnim.duration) {
                duration = viewAnim.duration
            }

//            if (null == interpolator) {
//                interpolator = viewAnim.getInterpolator();
//            }
            if (startDelay < viewAnim.startDelay) {
                startDelay = viewAnim.startDelay
            }
        }
        if (playerAnimators.isEmpty()) {
            return null
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(playerAnimators)
        animatorSet.startDelay = startDelay
        animatorSet.duration = duration
//        if (null != interpolator) {
//            animatorSet.setInterpolator(interpolator);
//        }
        animatorSet.addListener(object : Animator.AnimatorListener {
            private var isCanceled = false
            override fun onAnimationStart(animation: Animator) {
                isCanceled = false
                onStartListener?.onStart()
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isResetAnimation) {
                    val reversePlayerAnimators: MutableList<Animator> = ArrayList()
                    animations.filter { !it.keepEnd }.forEach {
                        val animators = it.getReverses()
                        if (animators.isNotEmpty()) {
                            reversePlayerAnimators.addAll(animators)
                        }
                    }

                    AnimatorSet().apply {
                        playTogether(reversePlayerAnimators)
                        duration = 100
                    }.start()
                }

                if (null == next) {
                    if (isCanceled) {
                        val cancelListener = _getOnCancelListener()
                        cancelListener?.onCancel()
                    } else {
                        val stopListener = _getOnStopListener()
                        stopListener?.onStop()
                    }
                } else {
                    next?.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                isCanceled = true
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        return animatorSet
    }

    private fun _getOnStopListener(): OnStop? {
        return if (null != prev) {
            prev?._getOnStopListener()
        } else {
            onStopListener
        }
    }

    private fun _getOnCancelListener(): OnCancel? {
        return if (null != prev) {
            prev?._getOnCancelListener()
        } else {
            onCancelListener
        }
    }

    fun setRepeatCount(count: Int) {
        repeatCount = count
    }

    fun setRepeatMode(mode: Int) {
        repeatMode = mode
    }

    fun clear() {
        cancel()
        animations.clear()
    }

    companion object {
        const val INFINITE: Int = ObjectAnimator.INFINITE
    }
}