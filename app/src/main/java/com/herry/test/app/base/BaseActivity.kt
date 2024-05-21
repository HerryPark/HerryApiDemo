package com.herry.test.app.base

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.herry.libs.app.activity_caller.activity.ACActivity
import com.herry.libs.helper.ApiHelper
import com.herry.libs.permission.PermissionHelper
import com.herry.libs.util.AppActivityManager
import com.herry.libs.util.AppUtil
import com.herry.libs.util.FragmentAddingOption
import com.herry.libs.util.OnSoftKeyboardVisibilityListener
import com.herry.libs.util.ViewUtil
import com.herry.libs.util.listener.ListenerRegistry

abstract class BaseActivity : ACActivity() {

    @IdRes
    open fun getHostViewId(): Int? = null

    @LayoutRes
    protected open fun getContentViewId(): Int = 0

    open fun getStartFragment(): Fragment? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackKeyPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // super.onBackPressed() is deprecated from API 33
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val contentViewId = getContentViewId()
        if (contentViewId != 0) {
            createContentView(contentViewId)
        }

        ViewUtil.setSoftKeyboardVisibilityListener(this, onSoftKeyboardVisibilityListener)
    }

    private fun createContentView(@LayoutRes id: Int) {
        onPreSetContentView()
        setContentView(id)
        onPostSetContentView()

        getStartFragment()?.run {
            AppUtil.setFragment(this@BaseActivity, getHostViewId(),
                this,
                FragmentAddingOption(isReplace = true, isAddToBackStack = true)
            )
        }
    }

    protected open fun onPreSetContentView() { }

    protected open fun onPostSetContentView() { }

    override fun onResume() {
        super.onResume()

        // checks changed application by user
        if ((application is BaseApplication) && (application as BaseApplication).isNeedRestartApp()) {
            (application as BaseApplication).resetRestartApp()
            // restart application
            packageManager.getLaunchIntentForPackage(packageName)?.run {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(this)
            }
        }
    }

    override fun getBlockedPermissionPopup(permissions: Array<String>, onCancel: ((dialog: DialogInterface) -> Unit)?): Dialog? {
        return PermissionHelper.createPermissionSettingScreenPopup(this, onCancel)?.getDialog()
    }

    protected fun finish(withoutAnimation: Boolean) {
        super.finish()
        if (withoutAnimation) {
            if (ApiHelper.hasAPI34()) {
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
    }

    open fun getActivityManager(): AppActivityManager? {
        return (application as? BaseApplication)?.getAppActivityManager()
    }

    private fun onBackKeyPressed() {
        val backStackFragment = AppUtil.getLastBackStackFragment(supportFragmentManager)
        if (null != backStackFragment) {
            val fragment = backStackFragment.fragment as? BaseFragment

            if (fragment?.onBackPressed() == true) {
                return
            }
        }

        super.onBackPressed()
    }

    private val onSoftKeyboardVisibilityListener = object: OnSoftKeyboardVisibilityListener {
        override fun onChangedSoftKeyboardVisibility(isVisible: Boolean) {
            softKeyboardVisibilityListeners.notifyListeners(object : ListenerRegistry.NotifyCB<OnSoftKeyboardVisibilityListener> {
                override fun notify(listener: OnSoftKeyboardVisibilityListener) {
                    // notify to child fragments
                    listener.onChangedSoftKeyboardVisibility(isVisible)
                    // notify to an inheritance activity
                    this@BaseActivity.onChangedSoftKeyboardVisibility(isVisible)
                }
            })
        }
    }

    /**
     * Call-back for the change soft keyboard visibility
     */
    protected open fun onChangedSoftKeyboardVisibility(isVisible: Boolean) { }

    private val softKeyboardVisibilityListeners = ListenerRegistry<OnSoftKeyboardVisibilityListener>()

    /**
     * adds the soft keyboard visibility changed listener for the Fragment, this is called from the BaseFragment's onCreate()
     */
    internal fun addOnSoftKeyboardVisibilityListener(listener: OnSoftKeyboardVisibilityListener) {
        softKeyboardVisibilityListeners.register(listener)
    }

    /**
     * removes the soft keyboard visibility changed listener for the Fragment, this is called from the BaseFragment's onDestroy()
     */
    internal fun removeOnSoftKeyboardVisibilityListener(listener: OnSoftKeyboardVisibilityListener) {
        softKeyboardVisibilityListeners.unregister(listener)
    }
}