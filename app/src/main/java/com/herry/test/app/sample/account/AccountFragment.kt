package com.herry.test.app.sample.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.libs.util.AppUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setViewMarginTop
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.TitleBarForm

class AccountFragment : BaseNavFragment() {

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.account_fragment, container, false)
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

        view.findViewById<View?>(R.id.account_fragment_title)?.apply {

        }

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById<View?>(R.id.account_fragment_title)?.apply {
                this.setViewMarginTop(ViewUtil.getStatusBarHeight(context))
            })
            bindFormModel(view.context, TitleBarForm.Model(backEnable = true))
        }
    }
}