package com.herry.test.app.base

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.TransitionRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.herry.libs.app.activity_caller.AC
import com.herry.libs.helper.TransitionHelper
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.view.viewgroup.LoadingCountView
import java.lang.ref.WeakReference

open class BaseFragment : DialogFragment() {

    internal open var activityCaller: AC? = null

    internal val fragmentTag: String = createTag()

    companion object {
        private const val TAG = "ARG_TAG"
    }

    protected open fun onScreenWindowStyle(context: Context): ScreenWindowStyle? = null

    private fun createTag(): String = "${this::class.java.simpleName}#${System.currentTimeMillis()}"

    protected open fun createArguments(): Bundle = bundleOf(TAG to fragmentTag)

    protected fun getDefaultArguments(): Bundle {
        return arguments ?: Bundle()
    }

    fun setDefaultArguments(bundle: Bundle) {
        this.arguments = bundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transitionHelper.onCreate(activity, this)
    }

    override fun onDestroy() {
        super.onDestroy()

        transitionHelper.onDestroy(activity)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activityCaller = if(context is AC) {
            context
        } else {
            null
        }
    }

    override fun onDetach() {
        activityCaller = null
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()

        val context = this.context ?: return
        val defaultScreenWindowStyle = ScreenWindowStyle(isFullScreen = ViewUtil.isSystemFullScreen(context))
        val screenStyle = if (ViewUtil.isPortraitOrientation(context)) {
            onScreenWindowStyle(context) ?: defaultScreenWindowStyle
        } else {
            defaultScreenWindowStyle
        }

        screenStyle.let { windowStyle ->
            ViewUtil.makeFullScreen(activity, windowStyle.isFullScreen)
            when (windowStyle.statusBarStyle) {
                StatusBarStyle.LIGHT -> ViewUtil.setStatusBarTransparent(activity, mode = ViewUtil.StatusBarMode.LIGHT)
                StatusBarStyle.DARK -> ViewUtil.setStatusBarTransparent(activity, mode = ViewUtil.StatusBarMode.DARK)
                null -> {
                    val typedValue = TypedValue()
                    val attrs: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.statusBarColor))
                    val color = attrs.getColor(0, 0)
                    ViewUtil.setStatusBarColor(activity, color)
                    attrs.recycle()
                }
            }
        }
    }

    open fun onBackPressed(): Boolean = false

    private var loading: LoadingCountView? = null

    protected open fun showLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showMainLoopLoading()
        } else {
            Handler(Looper.getMainLooper()).post {
                showMainLoopLoading()
            }
        }
    }

    private fun showMainLoopLoading() {
        if (loading == null) {
            loading = context?.run {
                LoadingCountView(this).apply {
                }
            }

            loading?.let {
                it.visibility = View.GONE

                if (view is FrameLayout) {
                    (view as FrameLayout).addView(it, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                } else if (view is ConstraintLayout) {
                    val layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                        startToStart = ConstraintSet.PARENT_ID
                        endToEnd = ConstraintSet.PARENT_ID
                        topToTop = ConstraintSet.PARENT_ID
                        bottomToBottom = ConstraintSet.PARENT_ID
                    }
                    (view as ConstraintLayout).addView(it, layoutParams)
                }
            }
        }

        loading?.show()
    }

    protected open fun hideLoading(force: Boolean = false) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            loading?.hide(force)
        } else {
            Handler(Looper.getMainLooper()).post {
                loading?.hide(force)
            }
        }
    }

    protected val transitionHelper by lazy {
        TransitionHelper(
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            listener = WeakReference(object : TransitionHelper.TransitionHelperListener {
                override fun onTransitionStart() {
                    this@BaseFragment.onTransitionStart()
                }

                override fun onTransitionEnd() {
                    this@BaseFragment.onTransitionEnd()
                }
            })
        )
    }

    @TransitionRes
    protected open val enterTransition: Int = 0

    @TransitionRes
    protected open val exitTransition: Int = 0

    protected open fun onTransitionStart() {
    }

    protected open fun onTransitionEnd() {
    }
}