package com.herry.test.app.font.downloadable

import android.graphics.Typeface
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
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.libs.widget.view.AppButton
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.widget.TitleBarForm
import java.util.Locale

class DownloadableFontFragment: BaseNavView<DownloadableFontContract.View, DownloadableFontContract.Presenter>(), DownloadableFontContract.View {
    override fun onCreatePresenter(): DownloadableFontContract.Presenter = DownloadableFontPresenter()

    override fun onCreatePresenterView(): DownloadableFontContract.View = this

    private var  container: View? = null

    private var appliedTextView: TextView? = null

    private var fontListView: RecyclerView? = null

    private var applyButton: AppButton? = null

    private var loadingView: View? = null

    private val fontsAdapter: Adapter = Adapter()

    override val fontsRoot: NodeRoot
        get() = fontsAdapter.root

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.downloadable_font_fragment, container, false)?.apply { init(this) }?.also { this.container = it }
    }

    private fun init(view: View) {
        val context = view.context
        TitleBarForm(activity = { requireActivity() }).apply {
            bindFormHolder(context, view.findViewById(R.id.downloadable_font_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Downloadable Font", backEnable = true))
        }

        fontListView = view.findViewById<RecyclerView?>(R.id.downloadable_font_fragment_fonts)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@DownloadableFontFragment.fontsAdapter
        }

        appliedTextView = view.findViewById(R.id.downloadable_font_fragment_applied_font)
        applyButton = view.findViewById<AppButton?>(R.id.downloadable_font_fragment_apply)?.apply {
            setOnSingleClickListener { presenter?.applyFont() }
        }

        loadingView = view.findViewById(R.id.downloadable_font_fragment_loading)
    }

    override fun onUpdateText(fontName: String, typeface: Typeface?) {
        appliedTextView?.let { textView ->
            textView.text = fontName
            textView.typeface = typeface
        }
    }

    override fun onLoading(show: Boolean) {
        loadingView?.isVisible = show
        applyButton?.isEnabled = !show
    }

    override fun onFailed(reason: Int) {
        ToastHelper.showToast(activity, String.format(Locale.getDefault(), "Font request failed with reason code: %d", reason))
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FontListItemForm(
                onClickItem = { form, holder ->
                    NodeRecyclerForm.getBindModel(form, holder)?.let { model ->
                        presenter?.selectFont(model)
                    }
                }
            ))
        }
    }
}