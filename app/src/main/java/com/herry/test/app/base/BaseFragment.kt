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
import com.herry.libs.annotation.OrientationScreen
import com.herry.libs.app.activity_caller.AC
import com.herry.libs.helper.TransitionHelper
import com.herry.libs.util.OnSoftKeyboardVisibilityListener
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUI
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.view.viewgroup.LoadingCountView
import java.lang.ref.WeakReference

open class BaseFragment : DialogFragment() {
    internal open var activityCaller: AC? = null

    internal val fragmentTag: String = createTag()

    companion object {
        private const val TAG = "ARG_TAG"
    }

    private var defaultSystemUIAppearances: SystemUIAppearances? = null

    /**
     * Sets the fragment screen's system ui style
     * @return null is keeps the current applied system ui styles (status bar, navigation bar)
     */
    protected open fun onSystemUIAppearances(context: Context): SystemUIAppearances? = null

    private fun createTag(): String = "${this::class.java.simpleName}#${System.currentTimeMillis()}"

    protected open fun createArguments(): Bundle = bundleOf(TAG to fragmentTag)

    protected fun getDefaultArguments(): Bundle {
        return arguments ?: Bundle()
    }

    fun setDefaultArguments(bundle: Bundle) {
        this.arguments = bundle
    }

    private var isSoftKeyboardVisible = false

    private val onSoftKeyboardVisibilityListener = object : OnSoftKeyboardVisibilityListener {
        override fun onChangedSoftKeyboardVisibility(isVisible: Boolean) {
            if (isSoftKeyboardVisible != isVisible) {
                isSoftKeyboardVisible = isVisible
                this@BaseFragment.onChangedSoftKeyboardVisibility(isVisible)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as? BaseActivity)?.let { activity ->
            isSoftKeyboardVisible = ViewUtil.isSoftKeyboardShown(activity)
            onChangedSoftKeyboardVisibility(isSoftKeyboardVisible)

            activity.addOnSoftKeyboardVisibilityListener(onSoftKeyboardVisibilityListener)
        }
    }

    protected open fun onChangedSoftKeyboardVisibility(isVisible: Boolean) {}

    override fun onDestroy() {
        super.onDestroy()

        (activity as? BaseActivity)?.removeOnSoftKeyboardVisibilityListener(onSoftKeyboardVisibilityListener)

        transitionHelper.onDestroy(activity)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activityCaller = if (context is AC) {
            context
        } else {
            null
        }
    }

    override fun onDetach() {
        activityCaller = null
        super.onDetach()
    }

    private fun applySystemUiAppearances(appearances: SystemUIAppearances) {
        val activity = this.activity ?: return
        SystemUI.setSystemUiVisibility(
            activity = activity,
            isFull = appearances.isFullScreen,
            showBehavior = appearances.showBehavior,
            statusBarVisibility = appearances.statusBar?.visibility,
            navigationBarVisibility = appearances.navigationBar?.visibility
        )
        appearances.statusBar?.let { statusBar ->
            SystemUI.setStatusBar(activity = activity, appearance = statusBar)
        }
        appearances.navigationBar?.let { navigationBar ->
            SystemUI.setNavigationBar(activity = activity, appearance = navigationBar)
        }
    }

    override fun onResume() {
        super.onResume()

        val context = this.context ?: return

        if (defaultSystemUIAppearances == null) {
            onSystemUIAppearances(context)?.let { instanceSystemUIAppearances ->
                defaultSystemUIAppearances = SystemUIAppearances.getDefaultSystemUIAppearances(context) // default system ui appearances
                applySystemUiAppearances(instanceSystemUIAppearances)
            }
        }
    }

    override fun onPause() {
        defaultSystemUIAppearances?.let { systemUIAppearances ->
            // removes instance system ui appearances
            applySystemUiAppearances(systemUIAppearances)
            defaultSystemUIAppearances = null
        }

        // hide loading view
        loading?.hide(true)
        super.onPause()


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

    protected fun getOrientation(): Int {
        return if (ViewUtil.isPortraitOrientation(context)) OrientationScreen.PORTRAIT else OrientationScreen.LANDSCAPE
    }
}