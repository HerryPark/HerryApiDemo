package com.herry.test.app.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView
import com.herry.test.widget.TitleBarForm

class TimelineFragment : BaseMVPNavView<TimelineContract.View, TimelineContract.Presenter>(), TimelineContract.View {
    override fun onCreatePresenter(): TimelineContract.Presenter = TimelinePresenter()

    override fun onCreatePresenterView(): TimelineContract.View = this

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.timeline_fragment, container, false)?.apply { init(this) }?.also { this.container = it }
    }

    private fun init(view: View) {
        TitleBarForm(activity = { requireActivity() }).apply {
            bindFormHolder(view.context, view.findViewById(R.id.timeline_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Timeline", backEnable = true))
        }
    }
}