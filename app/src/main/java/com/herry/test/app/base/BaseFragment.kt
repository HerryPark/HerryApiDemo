package com.herry.test.app.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.TransitionRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.herry.libs.annotation.OrientationScreen
import com.herry.libs.app.activity_caller.AC
import com.herry.libs.helper.TransitionHelper
import com.herry.libs.log.Trace
import com.herry.libs.util.KeyboardUtil
import com.herry.libs.util.OnSoftKeyboardVisibilityChangedListener
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUI
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.view.viewgroup.LoadingCountView
import com.herry.test.R
import java.lang.ref.WeakReference

@Suppress("unused")
open class BaseFragment : DialogFragment {
    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    internal open var activityCaller: AC? = null

    private val screenName: String = this.getScreenName()

    internal val fragmentTag: String = createTag()

    companion object {
        private const val TAG = "ARG_TAG"
    }

    private var restoreSystemUIAppearances: SystemUIAppearances? = null

    /**
     * Sets the fragment screen's system ui style
     * @return null is keeps the current applied system ui styles (status bar, navigation bar)
     */
    protected open fun onSystemUIAppearances(context: Context, default: SystemUIAppearances): SystemUIAppearances? = null

    protected open fun getScreenName(): String = this::class.java.simpleName

    private fun createTag(): String = "${this::class.java.simpleName}#${System.currentTimeMillis()}"

    protected open fun createArguments(): Bundle = bundleOf(TAG to fragmentTag)

    protected fun getDefaultArguments(): Bundle {
        return arguments ?: Bundle()
    }

    fun setDefaultArguments(bundle: Bundle) {
        this.arguments = bundle
    }

    private var isSoftKeyboardVisible = false

    private val onSoftKeyboardVisibilityChangedListener = object : OnSoftKeyboardVisibilityChangedListener {
        override fun onChanged(isVisible: Boolean) {
            if (isSoftKeyboardVisible != isVisible) {
                isSoftKeyboardVisible = isVisible
                this@BaseFragment.onChangedSoftKeyboardVisibility(isVisible)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as? BaseActivity)?.let { activity ->
            isSoftKeyboardVisible = KeyboardUtil.isSoftKeyboardShown(activity)
            onChangedSoftKeyboardVisibility(isSoftKeyboardVisible)

            activity.addOnSoftKeyboardVisibilityChangedListener(onSoftKeyboardVisibilityChangedListener)

            transitionHelper.onCreate(activity, this)
        }
    }

    protected open fun onChangedSoftKeyboardVisibility(isVisible: Boolean) { }

    override fun onDestroy() {
        super.onDestroy()

        (activity as? BaseActivity)?.removeOnSoftKeyboardVisibilityChangedListener(onSoftKeyboardVisibilityChangedListener)

        transitionHelper.onDestroy(activity)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activityCaller = context as? AC
    }

    override fun onDetach() {
        activityCaller = null
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun applySystemUiAppearances(appearances: SystemUIAppearances) {
        val activity = this.activity ?: return

        SystemUI.setSystemUiVisibility(
            activity = activity,
            isFull = appearances.isFullScreen,
            showBehavior = appearances.showBehavior,
            statusBarVisibility = appearances.statusBar?.visibility,
            navigationBarVisibility = appearances.navigationBar?.visibility,
            softInputMode = appearances.softInputMode
        )
        appearances.statusBar?.let { statusBar ->
            SystemUI.setStatusBar(activity = activity, appearance = statusBar)
        }
        appearances.navigationBar?.let { navigationBar ->
            SystemUI.setNavigationBar(activity = activity, appearance = navigationBar)
        }
    }

    override fun onStart() {
        super.onStart()

        val activity = this.activity ?: return

        // default system ui appearances
        val defaultSystemUIAppearances = SystemUIAppearances.getDefaultSystemUIAppearances(activity)
        onSystemUIAppearances(activity, defaultSystemUIAppearances)?.let { instanceSystemUIAppearances ->
            restoreSystemUIAppearances = defaultSystemUIAppearances
            Trace.d("apply fragment system UI appearances to $screenName")
            applySystemUiAppearances(instanceSystemUIAppearances)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        // hide loading view
        loading?.hide(true)
        super.onPause()
    }

    override fun onStop() {
        restoreSystemUIAppearances?.let { systemUIAppearances ->
            Trace.d("restore fragment system UI appearances to $screenName")
            // removes instance system ui appearances
            applySystemUiAppearances(systemUIAppearances)
        }

        super.onStop()
    }

    open fun onBackPressed(): Boolean = false

    private var loading: LoadingCountView? = null

    protected open fun showLoading() {
        val bgColor = getLoadingBackgroundColor()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showMainLoopLoading(bgColor)
        } else {
            Handler(Looper.getMainLooper()).post {
                showMainLoopLoading(bgColor)
            }
        }
    }

    @ColorInt
    protected open fun getLoadingBackgroundColor(): Int = "#80000000".toColorInt()

    private fun showMainLoopLoading(@ColorInt bgColor: Int) {
        val context = this.context ?: return
        (loading ?: (ViewUtil.inflate(context, R.layout.base_loading_view) as? LoadingCountView)?.apply {
            setBackgroundColor(bgColor)
            ViewUtil.setProtectTouchLowLayer(this, true)

            this.visibility = View.GONE

            val containerView = this@BaseFragment.view
            if (containerView is FrameLayout) {
                containerView.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            } else if (containerView is ConstraintLayout) {
                val layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    startToStart = ConstraintSet.PARENT_ID
                    endToEnd = ConstraintSet.PARENT_ID
                    topToTop = ConstraintSet.PARENT_ID
                    bottomToBottom = ConstraintSet.PARENT_ID
                }
                containerView.addView(this, layoutParams)
            }
        }.also { this.loading = it })?.show()
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
        return if (ViewUtil.isPortraitOrientation()) OrientationScreen.PORTRAIT else OrientationScreen.LANDSCAPE
    }

    protected open fun isOrientationPortrait(): Boolean = getOrientation() == OrientationScreen.PORTRAIT

    protected open fun isOrientationLandscape(): Boolean = !isOrientationPortrait()
}