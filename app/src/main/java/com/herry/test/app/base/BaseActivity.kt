package com.herry.test.app.base

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.herry.libs.app.activity_caller.activity.ACActivity
import com.herry.libs.permission.PermissionHelper
import com.herry.libs.util.AppActivityManager
import com.herry.libs.util.AppUtil
import com.herry.libs.util.FragmentAddingOption
import com.herry.libs.util.KeyboardUtil
import com.herry.libs.util.OnSoftKeyboardVisibilityChangedListener
import com.herry.libs.util.listener.ListenerRegistry
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Suppress("unused")
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

        addOnSoftKeyboardVisibilityChecker(onSoftKeyboardVisibilityChecker)
    }

    override fun onDestroy() {
        removeOnSoftKeyboardVisibilityChecker(onSoftKeyboardVisibilityChecker)
        super.onDestroy()
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

    override fun getBlockedPermissionPopup(permissions: Array<String>, onCancel: ((dialog: DialogInterface) -> Unit)?): Dialog? {
        return PermissionHelper.createPermissionSettingScreenPopup(activity = this, onCancel = onCancel)?.getDialog()
    }

    protected fun finish(withoutAnimation: Boolean) {
        super.finish()
        if (withoutAnimation) overridePendingTransition(0, 0)
    }

    open fun getActivityManager(): AppActivityManager? {
        return (application as? AppActivityManager.OnGetAppActivityManager)?.getAppActivityManager()
    }

    protected open fun onBackKeyPressed(): Boolean {
        val backStackFragment = AppUtil.getLastBackStackFragment(supportFragmentManager)
        when (val fragment = backStackFragment?.fragment) {
            is BaseFragment -> if (fragment.onBackPressed()) {
                return true
            }
        }

        // finish activity
        finish()
        return true
    }

    private val onSoftKeyboardVisibilityChecker = object : OnGlobalLayoutListener {
        private val wasShown: AtomicReference<Boolean> = AtomicReference()

        override fun onGlobalLayout() {
            val isShown = KeyboardUtil.isSoftKeyboardShown(this@BaseActivity)
            if (isShown == wasShown.get()) {
                // Keyboard state hasn't changed
                return
            }

            wasShown.set(isShown)

            softKeyboardVisibilityListeners.notifyListeners(object : ListenerRegistry.NotifyCB<OnSoftKeyboardVisibilityChangedListener> {
                override fun notify(listener: OnSoftKeyboardVisibilityChangedListener) {
                    // notify to child fragments
                    listener.onChanged(isShown)
                    // notify to an inheritance activity
                    this@BaseActivity.onChangedSoftKeyboardVisibility(isShown)
                }
            })
        }
    }

    private fun addOnSoftKeyboardVisibilityChecker(checker: OnGlobalLayoutListener) {
        val contentView = findViewById<View?>(android.R.id.content) ?: return
        contentView.viewTreeObserver?.addOnGlobalLayoutListener(checker)
    }

    private fun removeOnSoftKeyboardVisibilityChecker(checker: OnGlobalLayoutListener) {
        val contentView = findViewById<View?>(android.R.id.content) ?: return
        contentView.viewTreeObserver?.removeOnGlobalLayoutListener(checker)
    }

    /**
     * Call-back for the change soft keyboard visibility
     */
    protected open fun onChangedSoftKeyboardVisibility(isVisible: Boolean) { }

    private val softKeyboardVisibilityListeners = ListenerRegistry<OnSoftKeyboardVisibilityChangedListener>()

    /**
     * adds the soft keyboard visibility changed listener for the Fragment, this is called from the BaseFragment's onCreate()
     */
    internal fun addOnSoftKeyboardVisibilityChangedListener(listener: OnSoftKeyboardVisibilityChangedListener) {
        softKeyboardVisibilityListeners.register(listener)
    }

    /**
     * removes the soft keyboard visibility changed listener for the Fragment, this is called from the BaseFragment's onDestroy()
     */
    internal fun removeOnSoftKeyboardVisibilityChangedListener(listener: OnSoftKeyboardVisibilityChangedListener) {
        softKeyboardVisibilityListeners.unregister(listener)
    }

    private val withoutSavedInstance: AtomicBoolean = AtomicBoolean(false)
    protected fun recreate(withoutSavedInstance: Boolean) {
        this.withoutSavedInstance.set(withoutSavedInstance)
        ActivityCompat.recreate(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!withoutSavedInstance.get()) {
            super.onSaveInstanceState(outState)
        }
    }

    override fun onResume() {
        super.onResume()
        withoutSavedInstance.set(false)
    }
}