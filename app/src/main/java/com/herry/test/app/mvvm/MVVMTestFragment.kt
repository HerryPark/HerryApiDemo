package com.herry.test.app.mvvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.mvvm.BaseMVVMView
import com.herry.test.widget.TitleBarForm

class MVVMTestFragment : BaseMVVMView<MVVMTestViewModel>(MVVMTestViewModel::class.java) {
    private var container: View? = null
    private var counts: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.mvvm_test_fragment, container, false)?.apply { init(this) }?.also { this.container = it }
    }

    private fun init(view: View) {
        val context = view.context
        TitleBarForm(activity = { requireActivity() }).apply {
            bindFormHolder(context, view.findViewById(R.id.mvvm_test_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "MVVM Test", backEnable = true))
        }

        counts = view.findViewById(R.id.mvvm_test_fragment_counts)

        view.findViewById<View>(R.id.mvvm_test_fragment_minus)?.setOnSingleClickListener {
            viewmodel.decrease()
        }

        view.findViewById<View>(R.id.mvvm_test_fragment_plus)?.setOnSingleClickListener {
            viewmodel.increase()
        }

        viewmodel.counts.observe(this) {
            val count = it ?: return@observe
            counts?.text = count.toString()
        }
    }
}