package com.herry.test.app.widgets.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.navigateTo
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class WidgetsFragment: BaseNavView<WidgetsContract.View, WidgetsContract.Presenter>(), WidgetsContract.View {
    override fun onCreatePresenter(): WidgetsContract.Presenter = WidgetsPresenter()

    override fun onCreatePresenterView(): WidgetsContract.View = this


    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.widgets_fragment, container, false)?.also {
            this.container = it
            init(it)
        }
    }

    private fun init(view: View) {
        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.widgets_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Widgets", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.widgets_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@WidgetsFragment.adapter
        }
    }

    inner class Adapter : NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(WidgetItemForm { item ->
                navigateTo(destinationId = when (item) {
                    WidgetsContract.Widget.SPINNERS -> R.id.spinners_fragment
                })
            })
        }
    }

    private inner class WidgetItemForm(val onClickItem: (item: WidgetsContract.Widget) -> Unit): NodeForm<WidgetItemForm.Holder, WidgetsContract.Widget>(Holder::class, WidgetsContract.Widget::class) {
        inner class Holder(context: Context, view: View): NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@WidgetItemForm, this@Holder)?.let { item ->
                        onClickItem(item)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.main_test_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: WidgetsContract.Widget) {
            holder.title?.text = when (model) {
                WidgetsContract.Widget.SPINNERS -> "Spinners"
            }
        }
    }
}