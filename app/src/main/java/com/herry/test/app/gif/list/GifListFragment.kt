package com.herry.test.app.gif.list

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
import com.herry.test.app.gif.decoder.GifDecoderFragment
import com.herry.test.data.GifMediaFileInfoData
import com.herry.test.widget.TitleBarForm

/**
 * Created by herry.park on 2020/06/11.
 **/
class GifListFragment : BaseNavView<GifListContract.View, GifListContract.Presenter>(), GifListContract.View {

    override fun onCreatePresenter(): GifListContract.Presenter = GifListPresenter()

    override fun onCreatePresenterView(): GifListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.gif_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.gif_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Gif List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.gif_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@GifListFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FileListItemForm())
        }
    }

    override fun onDetail(content: GifMediaFileInfoData) {
        navigateTo(destinationId = R.id.gif_decoder_fragment, args = Bundle().apply {
            putSerializable(GifDecoderFragment.ARG_GIF_INFO_DATA, content)
        })
    }

    private inner class FileListItemForm : NodeForm<FileListItemForm.Holder, GifMediaFileInfoData>(Holder::class, GifMediaFileInfoData::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val name: TextView? = view.findViewById(R.id.file_list_item_name)

            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@FileListItemForm, this@Holder)?.let {
                        presenter?.decode(it)
                    }
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.file_list_item

        override fun onBindModel(context: Context, holder: Holder, model: GifMediaFileInfoData) {
            holder.name?.text = model.name
        }
    }
}