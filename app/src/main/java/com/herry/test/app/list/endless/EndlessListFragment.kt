package com.herry.test.app.list.endless

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
import com.herry.libs.widget.view.recyclerview.endless.EndlessRecyclerViewScrollListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class EndlessListFragment : BaseNavView<EndlessListContract.View, EndlessListContract.Presenter>(), EndlessListContract.View {
    override fun onCreatePresenter(): EndlessListContract.Presenter = EndlessListPresenter()

    override fun onCreatePresenterView(): EndlessListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Endless List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }

            this@apply.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    presenter?.loadMore()
                }
            })

            adapter = this@EndlessListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm())
        }
    }

    private inner class ListItemForm : NodeForm<ListItemForm.Holder, EndlessListContract.ListItemData>(
        Holder::class, EndlessListContract.ListItemData::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.list_item_name)
        }

        override fun onLayout(): Int = R.layout.list_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: EndlessListContract.ListItemData) {
            holder.title?.text = model.name
        }
    }
}