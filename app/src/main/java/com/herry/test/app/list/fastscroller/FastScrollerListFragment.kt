package com.herry.test.app.list.fastscroller

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
import com.herry.libs.widget.view.recyclerview.scroller.RecyclerViewFastScrollerView
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView
import com.herry.test.widget.TitleBarForm

class FastScrollerListFragment: BaseMVPNavView<FastScrollerListContract.View, FastScrollerListContract.Presenter>(), FastScrollerListContract.View {

    override fun onCreatePresenter(): FastScrollerListContract.Presenter = FastScrollerListPresenter()

    override fun onCreatePresenterView(): FastScrollerListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.fast_scroller_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.fast_scroller_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Fast Scroller List", backEnable = true))
        }

        val recyclerViewFastScroller = view.findViewById<RecyclerViewFastScrollerView>(R.id.fast_scroller_list_fragment_rvfs)

        view.findViewById<RecyclerView>(R.id.fast_scroller_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@FastScrollerListFragment.adapter

            recyclerViewFastScroller?.attachRecyclerView(this)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm())
        }
    }

    private inner class ListItemForm : NodeForm<ListItemForm.Holder, FastScrollerListContract.ListItemData>(
        Holder::class, FastScrollerListContract.ListItemData::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.list_item_name)
        }

        override fun onLayout(): Int = R.layout.list_item


        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: FastScrollerListContract.ListItemData) {
            holder.title?.text = model.name
        }

    }
}