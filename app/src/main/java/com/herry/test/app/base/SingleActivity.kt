package com.herry.test.app.base

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.herry.test.R
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUIAppearance
import kotlin.reflect.full.createInstance

open class SingleActivity: BaseActivity() {

    companion object {
        const val FRAGMENT_CLASS_NAME = "FRAGMENT_CLASS_NAME"
        const val FRAGMENT_BUNDLE = "FRAGMENT_BUNDLE"
//        const val FRAGMENT_SYSTEM_UI_VISIBILITY = "FRAGMENT_SYSTEM_UI_VISIBILITY"
//        const val FRAGMENT_STATUS_BAR_COLOR = "FRAGMENT_STATUS_BAR_COLOR"
        const val FRAGMENT_TRANSPARENT_STATUS_BAR = "FRAGMENT_TRANSPARENT_STATUS_BAR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
//            val systemUiVisibility = it.getIntExtra(FRAGMENT_SYSTEM_UI_VISIBILITY, 0)
//            val statusBarColor = it.getIntExtra(FRAGMENT_STATUS_BAR_COLOR, 0)
//
//            window.decorView.systemUiVisibility = systemUiVisibility
//            if (0 < systemUiVisibility) {
//                window.statusBarColor = statusBarColor
//            }

            val transparentStatusBar = it.getBooleanExtra(FRAGMENT_TRANSPARENT_STATUS_BAR, false)
            ViewUtil.setStatusBar(this,
                SystemUIAppearance(backgroundColor = if (transparentStatusBar) Color.TRANSPARENT else null))
            ViewUtil.setSystemUiVisibility(this, transparentStatusBar)
        }

        setContentView(R.layout.activity_base)

        getBaseFragment()?.let {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.activity_base_container, it)
            fragmentTransaction.commit()
            return
        }

        finish()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.fragments.isNotEmpty()) {
            if(supportFragmentManager.fragments[0] is BaseFragment) {
                if((supportFragmentManager.fragments[0] as BaseFragment).onBackPressed()) {
                    return
                }
            }
        }

        super.onBackPressed()
    }

    protected open fun getBaseFragment(): Fragment? {
        intent?.let {
            val fragmentName = it.getStringExtra(FRAGMENT_CLASS_NAME)
            if(!fragmentName.isNullOrBlank()) {
                val cls = Class.forName(fragmentName).kotlin
                val obj = cls.createInstance()
                if(obj is BaseFragment) {
                    it.getBundleExtra(FRAGMENT_BUNDLE)?.let { bundle ->
                        obj.setDefaultArguments(bundle)
                    }
                    return obj
                }
            }
        }

        return null
    }
}