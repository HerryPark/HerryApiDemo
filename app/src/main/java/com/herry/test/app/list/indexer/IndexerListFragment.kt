package com.herry.test.app.list.indexer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.libs.widget.view.recyclerview.scroller.RecyclerViewAlphabetIndexerScrollerView
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class IndexerListFragment : BaseNavView<IndexerListContract.View, IndexerListContract.Presenter>(), IndexerListContract.View {
    override fun onCreatePresenter(): IndexerListContract.Presenter = IndexerListPresenter()

    override fun onCreatePresenterView(): IndexerListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.alphabet_indexer_scroller_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.alphabet_indexer_scroller_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Indexer List", backEnable = true))
        }

        val recyclerViewScroller = view.findViewById<RecyclerViewAlphabetIndexerScrollerView>(R.id.alphabet_indexer_scroller_list_fragment_rvais)

        view.findViewById<RecyclerView>(R.id.alphabet_indexer_scroller_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@IndexerListFragment.adapter

            recyclerViewScroller?.attachRecyclerView(this)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext), SectionIndexer {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm { model ->
                ToastHelper.run { showToast(requireActivity(), model.name) }
            })
        }

        override fun getSections(): Array<String> {
            return presenter?.getSections()?.toTypedArray() ?: ArrayList<String>().toTypedArray()
        }

        override fun getPositionForSection(sectionIndex: Int): Int {
            return presenter?.getPositionForSection(sectionIndex) ?: 0
        }

        override fun getSectionForPosition(position: Int): Int {
            return presenter?.getSectionForPosition(position) ?: 0
        }
    }

    private inner class ListItemForm(private val onClick: (model: IndexerListContract.ListItemData) -> Unit) : NodeForm<ListItemForm.Holder, IndexerListContract.ListItemData>(
        Holder::class, IndexerListContract.ListItemData::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.list_item_name)

            init {
                view.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@ListItemForm, this@Holder)?.let { model ->
                        onClick.invoke(model)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.list_item


        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: IndexerListContract.ListItemData) {
            holder.title?.text = model.name
        }

    }
}