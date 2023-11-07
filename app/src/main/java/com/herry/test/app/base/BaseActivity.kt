package com.herry.test.app.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.herry.libs.app.activity_caller.activity.ACActivity
import com.herry.libs.helper.ApiHelper
import com.herry.libs.util.AppActivityManager
import com.herry.libs.util.AppUtil
import com.herry.libs.util.FragmentAddingOption
import com.herry.libs.permission.PermissionHelper

@Suppress("PrivatePropertyName")
abstract class BaseActivity : ACActivity() {

    @IdRes
    open fun getHostViewID(): Int? = null

    @LayoutRes
    open fun getContentViewID(): Int = -1

    open fun getStartFragment(): Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getContentViewID() != -1) {
            createContentView()
        }
    }

    private fun createContentView() {
        setContentView(getContentViewID())

        getStartFragment()?.run {
            AppUtil.setFragment(this@BaseActivity, getHostViewID(),
                this,
                FragmentAddingOption(isReplace = true, isAddToBackStack = true)
            )
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    open fun onActivityOrientation() {
        if (ApiHelper.hasOreo()) {
            try {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } catch (ex: IllegalStateException) {
                // isTranslucentOrFloating
//                Trace.e("Oreo", "Only fullscreen opaque activities can request orientation")
            }

        } else {
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

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
        if (withoutAnimation) overridePendingTransition(0, 0)
    }

    open fun getActivityManager(): AppActivityManager? {
        return if (application is BaseApplication) {
            (application as BaseApplication).appActivityManager
        } else {
            null
        }
    }

    override fun onBackPressed() {
        val backStackFragment = AppUtil.getLastBackStackFragment(supportFragmentManager)
        if (null != backStackFragment) {
            val fragment = backStackFragment.fragment as? BaseFragment

            if (fragment?.onBackPressed() == true) {
                return
            }
        }

        super.onBackPressed()
    }
}