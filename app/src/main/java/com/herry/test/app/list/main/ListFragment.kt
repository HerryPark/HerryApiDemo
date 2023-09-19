package com.herry.test.app.list.main

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
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class ListFragment : BaseNavView<ListContract.View, ListContract.Presenter>(), ListContract.View {
    override fun onCreatePresenter(): ListContract.Presenter = ListPresenter()

    override fun onCreatePresenterView(): ListContract.View = this

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
            bindFormModel(view.context, TitleBarForm.Model(title = "List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@ListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm { type ->
                when(type) {
                    ListContract.Type.FAST_SCROLLER -> navigateTo(destinationId = R.id.fast_scroller_list_fragment)
                    ListContract.Type.ENDLESS -> navigateTo(destinationId = R.id.endless_list_fragment)
                    ListContract.Type.INDEXER -> navigateTo(destinationId = R.id.indexer_list_fragment)
                }
            })
        }
    }

    private inner class ListItemForm(private val onClick: (type: ListContract.Type) -> Unit) : NodeForm<ListItemForm.Holder, ListContract.Type>(
        Holder::class, ListContract.Type::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.list_item_name)

            init {
                title?.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@ListItemForm, this@Holder)?.let { type ->
                        onClick.invoke(type)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.list_item


        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: ListContract.Type) {
            holder.title?.text = model.name
        }
    }
}