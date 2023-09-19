package com.herry.test.app.tflite.list

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
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

class TFLiteListFragment : BaseNavView<TFLiteListContract.View, TFLiteListContract.Presenter>(), TFLiteListContract.View {
    override fun onCreatePresenter(): TFLiteListContract.Presenter = TFLiteListPresenter()

    override fun onCreatePresenterView(): TFLiteListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.tflite_list_fragment, container, false)?.apply { init(this) }.also { this.container = it}
    }

    private fun init(view: View) {
        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.tflite_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "TFLite List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.tflite_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@TFLiteListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ListItemForm { form, holder ->
                when (NodeRecyclerForm.getBindModel(form, holder)) {
                    TFLiteListContract.Item.DIGIT_CLASSIFIER -> {
                        navigateTo(destinationId = R.id.digit_classifier_fragment)
                    }
                    TFLiteListContract.Item.IMAGE_CLASSIFIER -> {
                        navigateTo(destinationId = R.id.image_classifier_fragment)
                    }
                    TFLiteListContract.Item.LIVE_CLASSIFIER -> {
                        navigateTo(destinationId = R.id.live_classifier_fragment)
                    }
                    null -> {}
                }
            })
        }
    }

    private inner class ListItemForm(
        val onClickItem: (form: ListItemForm, holder: ListItemForm.Holder) -> Unit
    ) : NodeForm<ListItemForm.Holder, TFLiteListContract.Item>(Holder::class, TFLiteListContract.Item::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnProtectClickListener {
                    onClickItem(this@ListItemForm, this@Holder)
                }
            }
        }

        override fun onLayout(): Int = R.layout.main_test_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: TFLiteListContract.Item) {
            holder.title?.text = model.name
        }
    }
}