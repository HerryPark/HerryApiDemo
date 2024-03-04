package com.herry.test.app.sample.me

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUIAppearanceColorStyle
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.configure.SystemUIShowBehavior
import com.herry.libs.widget.configure.SystemUIVisibility
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment

class MeFragment: BaseNavFragment() {

    override fun getSystemUIAppearances(context: Context): SystemUIAppearances =
        SystemUIAppearances.getDefaultSystemUIAppearances(context).apply {
            isFullScreen = true
            showBehavior = SystemUIShowBehavior.TRANSIENT_BARS_BY_SWIPE
            statusBar?.visibility = SystemUIVisibility.HIDE
        }

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.me_fragment, container, false)
            init(this.container)
        } else {
            // fixed: "java.lang.IllegalStateException: The specified child already has a parent.
            // You must call removeView() on the child's parent first."
            ViewUtil.removeViewFormParent(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return
    }
}