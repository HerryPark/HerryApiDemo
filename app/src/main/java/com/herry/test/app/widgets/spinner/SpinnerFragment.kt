package com.herry.test.app.widgets.spinner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.TitleBarForm

class SpinnerFragment: BaseNavFragment() {

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.spinners_fragment, container, false)?.also {
            this.container = it
            init(it)
        }
    }

    private fun init(view: View){
        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.spinners_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Spinners", backEnable = true))
        }
    }
}