package com.herry.test.app.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.herry.libs.widget.configure.SystemUI
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.extension.findParentNavHostFragment
import com.herry.libs.widget.view.viewgroup.LoadingCountView
import java.lang.ref.WeakReference

open class BaseFragment : DialogFragment() {

    internal open var activityCaller: AC? = null

    internal val fragmentTag: String = createTag()

    companion object {
        private const val TAG = "ARG_TAG"
    }

    /**
     * @return null is keeps the current applied system ui styles (status bar, navigation bar)
     */
    protected open fun getSystemUIAppearances(context: Context): SystemUIAppearances? {
        // a nested navigation fragment is not applies the system UI setting
        val parentFragment = findParentNavHostFragment()
        val isNestedChildFragment = parentFragment != null && parentFragment.findParentNavHostFragment() != null
        if (isNestedChildFragment) return null

        // creates system programmatically UI from the activity theme setting
        return SystemUIAppearances.getDefaultSystemUIAppearances(context) // default system ui appearances
    }

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
        getSystemUIAppearances(context)?.let { style ->
            SystemUI.setSystemUiVisibility(
                activity = activity,
                isFull = style.isFullScreen,
                showBehavior = style.showBehavior,
                statusBarVisibility = style.statusBar?.visibility,
                navigationBarVisibility = style.navigationBar?.visibility
            )
            style.statusBar?.let { statusBar ->
                SystemUI.setStatusBar(activity = activity, appearance = statusBar)
            }
            style.navigationBar?.let { navigationBar ->
                SystemUI.setNavigationBar(activity = activity, appearance = navigationBar)
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