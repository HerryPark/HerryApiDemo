package com.herry.test.app.intent.scheme

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.helper.ToastHelper
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm

/**
 * Created by herry.park on 2020/06/11.
 **/
class SchemeFragment : BaseNavView<SchemeContract.View, SchemeContract.Presenter>(), SchemeContract.View {

    override fun onCreatePresenter(): SchemeContract.Presenter = SchemePresenter()

    override fun onCreatePresenterView(): SchemeContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.scheme_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.scheme_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Scheme Intent", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.scheme_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@SchemeFragment.adapter
        }
    }

    override fun onGotoScheme(scheme: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(scheme)))
        } catch (ex: ActivityNotFoundException) {
            ToastHelper.showToast(activity, ex.message)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(SchemeItem2Form())
        }
    }

    private inner class SchemeItem2Form : NodeForm<SchemeItem2Form.Holder, SchemeContract.SchemaData>(Holder::class, SchemeContract.SchemaData::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.scheme_item_2_title)
            val appLink: TextView? = view.findViewById(R.id.scheme_item_2_app_link)
            val dynamicLink: TextView? = view.findViewById(R.id.scheme_item_2_dynamic_link)
            val shortLink: TextView? = view.findViewById(R.id.scheme_item_2_short_link)

            init {
                appLink?.setOnSingleClickListener {
                    NodeRecyclerForm.getBindModel(this@SchemeItem2Form, this@Holder)?.let {
                        presenter?.gotoScheme(it.appLink)
                    }
                }
                dynamicLink?.setOnSingleClickListener {
                    NodeRecyclerForm.getBindModel(this@SchemeItem2Form, this@Holder)?.let {
                        presenter?.gotoScheme(it.dynamicLink)
                    }
                }
                shortLink?.setOnSingleClickListener {
                    NodeRecyclerForm.getBindModel(this@SchemeItem2Form, this@Holder)?.let {
                        presenter?.gotoScheme(it.shortLink)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.scheme_item_2

        override fun onBindModel(context: Context, holder: Holder, model: SchemeContract.SchemaData) {
            holder.title?.text = model.title
            holder.appLink?.let { view ->
                view.text = model.appLink
                view.isVisible = model.appLink.isNotBlank()
            }
            holder.dynamicLink?.let { view ->
                view.text = model.dynamicLink
                view.isVisible = model.dynamicLink.isNotBlank()
            }
            holder.shortLink?.let { view ->
                view.text = model.shortLink
                view.isVisible = model.shortLink.isNotBlank()
            }
        }
    }
}