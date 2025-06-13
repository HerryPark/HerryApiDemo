package com.herry.test.app.base.nav

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.clearFragmentResultListener
import androidx.navigation.NavDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.findNavController
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.app.nav.NavMovement
import com.herry.libs.util.BundleUtil
import com.herry.libs.util.KeyboardUtil
import com.herry.libs.widget.extension.getNavCurrentDestinationID
import com.herry.libs.widget.extension.launchWhenViewResumed
import com.herry.libs.widget.extension.launchWhenViewStarted
import com.herry.libs.widget.extension.setFragmentResult
import com.herry.libs.widget.extension.setFragmentResultListener
import com.herry.test.app.base.BaseActivity
import com.herry.test.app.base.BaseFragment
import kotlinx.coroutines.*

@Suppress("SameParameterValue", "KDocUnresolvedReference")
open class BaseNavFragment : BaseFragment, NavMovement {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    companion object {
        @Suppress("ConstPropertyName")
        private const val NavigationID = "NavigationID"
    }
    /**
     * {@inheritDoc}
     *
     * @deprecated use
     * {@link #onNavigateUp()}
     */
    final override fun onBackPressed(): Boolean = false

    override fun onNavigateUp(): Boolean = false

    private fun setNavigateUpResult(result: Bundle) {
        if (activity is BaseNavActivity) {
            (activity as BaseNavActivity).setNavigationUpResult(result)
        }
    }

    override fun getNavigateUpResult(): Bundle = NavBundleUtil.createNavigationBundle(false)

    override fun onNavigateUpResult(@IdRes fromNavigationId: Int, result: Bundle) {}

    private var isOnNavigateUpDelay: Boolean = false

    protected fun navigateUp(resultOK: Boolean = false, result: Bundle? = null, force: Boolean = false, delayMs: Long = 0L) {
        navigateUp(NavBundleUtil.createNavigationBundle(resultOK, result), force, delayMs)
    }

    /**
     * finish fragment
     * @param result result value
     * @param force true is ignore blocked navigate up
     * @param delayMs action delay millisecond
     */
    protected fun navigateUp(result: Bundle? = null, force: Boolean = false, delayMs: Long = 0L) {
        launchWhenViewStarted {
            if (isOnNavigateUpDelay) {
                return@launchWhenViewStarted
            }
            if (delayMs > 0L && !force) {
                withContext(Dispatchers.Default) {
                    isOnNavigateUpDelay = true
                    delay(delayMs)
                    isOnNavigateUpDelay = false
                }
            }

            navigateUpInternal(result, force)
        }
    }

    private fun navigateUpInternal(result: Bundle? = null, force: Boolean = false) {
        if (!force && onNavigateUp()) {
            return
        }

        val navController = try {
            findNavController()
        } catch (_: Exception) {
            return
        }

        val currentNavDestination = navController.currentDestination
        val currentDestinationId = currentNavDestination?.id

        // sets from navigation id to result
        result?.apply {
            if (currentDestinationId != null) {
                NavBundleUtil.addFromNavigationId(result, currentDestinationId)
            }
        }

        if (isDialogTypeFragment(currentNavDestination)) {
            launchWhenViewResumed {
                navigateUpDialogFragment(result)
            }
            return
        }

        try {
            setNavigateUpResult((result ?: getNavigateUpResult().apply {
                // sets from navigation id to result
                if (currentDestinationId != null) {
                    NavBundleUtil.addFromNavigationId(this, currentDestinationId)
                }
            }))

            // calls system(navController) navigate up action
            if (!navController.navigateUp()) {
                finishActivity(NavBundleUtil.isNavigationResultOk(result), result)
            }
        } catch (_: IllegalStateException) {
            finishActivity(NavBundleUtil.isNavigationResultOk(result), result)
        }
    }

    // This function must be called from onResume.
    private fun navigateUpDialogFragment(bundle: Bundle? = null) {
        val notified = notifyNavigateUpResult(bundle)
        if (notified) {
            findNavController().popBackStack()
        }

        val dialog = super.getDialog()
        if (dialog != null) {
            dialog.cancel()
        } else {
            dismiss()
        }
    }

    fun notifyNavigateUpResult(bundle: Bundle? = null): Boolean {
        val navController = findNavController()
        val callNavigationId = navController.previousBackStackEntry?.destination?.id
        val currentDestinationId = navController.currentBackStackEntry?.destination?.id

        if (callNavigationId != null && currentDestinationId != null) {
            val result = bundle ?: getNavigateUpResult()
            NavBundleUtil.addFromNavigationId(result, currentDestinationId)
            setFragmentResult(
                callNavigationId.toString(),
                result
            )
            return true
        }

        return false
    }

    override fun isTransition(): Boolean = transitionHelper.isTransition()

    @IdRes
    private var navigationID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigationID = savedInstanceState?.getInt(NavigationID, 0) ?: run {
            if (activity is BaseNavActivity) {
                getNavCurrentDestinationID()
            } else {
                0
            }
        }
        val requestKey: String = if (navigationID == 0) super.fragmentTag else navigationID.toString()
        setFragmentResultListener(requestKey = requestKey, listener = { _, bundle ->
            val fromId = NavBundleUtil.fromNavigationId(bundle)
            onNavigateUpResult(fromId, bundle)
        })
    }

    override fun onDestroy() {
        val requestKey: String = if (navigationID == 0) super.fragmentTag else navigationID.toString()
        clearFragmentResultListener(requestKey)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NavigationID, navigationID)
    }

    /**
     * finish fragment.
     * If you want finish with to set result, creates [bundle] parameter.
     * @see BundleUtil.createNavigationBundle(Boolean)
     *
     * - Sets result to RESULT_OK
     *   finishActivity(BundleUtil.createNavigationBundle(true))
     * - Sets result to RESULT_CANCEL
     * finishActivity(null) or finishActivity(BundleUtil.createNavigationBundle(false))
     * @param bundle result data
     */
    protected fun finishFragment(bundle: Bundle?) {
        val activity = this.activity
        if (activity is BaseNavActivity) {
            KeyboardUtil.hideSoftKeyboard(fragment = this)
            navigateUp(bundle, force = true)
        } else if (activity is BaseActivity) {
            finishActivity(NavBundleUtil.isNavigationResultOk(bundle), bundle)
        }
    }

    /**
     * finish activity.
     * If you want finish with to set result, creates [bundle] parameter.
     * @see NavBundleUtil.createNavigationBundle(Boolean)
     * @param resultOK set result to ok or cancel
     * @param bundle result data
     */
    protected open fun finishActivity(resultOK: Boolean, bundle: Bundle? = null, afterTransition: Boolean = true) {
        activity?.let { activity ->
            KeyboardUtil.hideSoftKeyboard(fragment = this)

            val activityResult = if (resultOK) Activity.RESULT_OK else Activity.RESULT_CANCELED
            val resultBundle = if (null != bundle) {
                bundle.putBoolean(NavMovement.NAV_UP_RESULT_OK, resultOK)
                bundle
            } else {
                Bundle().apply {
                    putBoolean(NavMovement.NAV_UP_RESULT_OK, resultOK)
                }
            }
            activity.setResult(activityResult, Intent().apply {
                putExtra(NavMovement.NAV_BUNDLE, resultBundle)
            })
            if (afterTransition) {
                activity.finishAfterTransition()
            } else {
                activity.finish()
            }
        }
    }

    private fun isDialogTypeFragment(navDestination: NavDestination? = null): Boolean {
        val destination = navDestination ?: try {
            findNavController().currentDestination
        } catch (_: Exception) {
            null
        }

        return destination is DialogFragmentNavigator.Destination
    }

    private var isCanceled = false

    override fun onCancel(dialog: DialogInterface) {
        if (!isDialogTypeFragment()) return

        isCanceled = true
        // control dialog fragment's canceled action - send the navigation up result (false) to parent
        launchWhenViewResumed {
            navigateUpDialogFragment(NavBundleUtil.createNavigationBundle(false, null))
        }
    }

    /**
     * Cancel the dialog destination fragment. This fragment is defined to <dialog> destination in the navigation resource
     */
    protected fun cancel() {
        if (!isDialogTypeFragment()) return

        if (!isCanceled) {
            navigateUp()
        }
    }
}