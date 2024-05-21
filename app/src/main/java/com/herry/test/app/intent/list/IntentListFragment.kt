package com.herry.test.app.intent.list

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

/**
 * Created by herry.park on 2020/06/11.
 **/
class IntentListFragment : BaseNavView<IntentListContract.View, IntentListContract.Presenter>(), IntentListContract.View {

    override fun onCreatePresenter(): IntentListContract.Presenter = IntentListPresenter()

    override fun onCreatePresenterView(): IntentListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.intent_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.intent_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Intent List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.intent_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@IntentListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(TestItemForm())
        }
    }

    override fun onScreen(type: IntentListContract.TestItemType) {
        when (type) {
            IntentListContract.TestItemType.SCHEME_TEST -> {
                navigateTo(destinationId = R.id.scheme_fragment)
            }
            IntentListContract.TestItemType.MEDIA_SHARE_TEST -> {
                navigateTo(destinationId = R.id.share_media_list_fragment)
            }
        }
    }

    private inner class TestItemForm : NodeForm<TestItemForm.Holder, IntentListContract.TestItemType>(Holder::class, IntentListContract.TestItemType::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@TestItemForm, this@Holder)?.let {
                        presenter?.moveToScreen(it)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.main_test_item

        override fun onBindModel(context: Context, holder: TestItemForm.Holder, model: IntentListContract.TestItemType) {
            holder.title?.text = model.label
        }
    }
}