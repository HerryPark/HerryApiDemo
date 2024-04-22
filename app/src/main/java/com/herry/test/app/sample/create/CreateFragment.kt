package com.herry.test.app.sample.create

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUIAppearance
import com.herry.libs.widget.configure.SystemUIAppearanceColorStyle
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.configure.SystemUIVisibility
import com.herry.libs.widget.extension.*
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment

class CreateFragment: BaseNavFragment() {

    override fun onSystemUIAppearances(context: Context): SystemUIAppearances {
        return SystemUIAppearances.getDefaultSystemUIAppearances(context).apply {
            isFullScreen = true
            statusBar?.backgroundColor = Color.BLACK
            statusBar?.visibility = SystemUIVisibility.SHOW
        }
    }

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.create_fragment, container, false)
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

        view.findViewById<View>(R.id.create_fragment_setting)?.let { setting ->
            val margins = setting.getViewMargins()
            setting.setViewMarginTop(margins.top + ViewUtil.getStatusBarHeight(context))
            setting.setOnProtectClickListener {
                navigateTo(destinationId = R.id.setting_fragment)
            }
        }
    }
}