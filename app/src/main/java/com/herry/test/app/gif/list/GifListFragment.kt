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
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerViewEmptyTextForm
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerViewForm
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView
import com.herry.test.app.gif.decoder.GifDecoderFragment
import com.herry.libs.permission.PermissionHelper
import com.herry.test.data.GifMediaFileInfoData
import com.herry.test.widget.TitleBarForm

/**
 * Created by herry.park on 2020/06/11.
 **/
class GifListFragment : BaseMVPNavView<GifListContract.View, GifListContract.Presenter>(), GifListContract.View {

    override fun onCreatePresenter(): GifListContract.Presenter = GifListPresenter()

    override fun onCreatePresenterView(): GifListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    private var mediaListForm = object: RecyclerViewForm() {
        override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView) {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
                if (itemAnimator is SimpleItemAnimator) {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                adapter = this@GifListFragment.adapter
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.gif_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.gif_list_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Gif List", backEnable = true))
        }

        view.findViewById<View?>(R.id.gif_list_fragment_list)?.let {
            mediaListForm.bindHolder(context, it)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FileListItemForm())
        }
    }

    override fun onCheckPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: () -> Unit) {
        if (!PermissionHelper.hasPermission(context, type)) {
            activityCaller?.call(
                ACPermission.Caller(
                    permissions = type.permissions.toTypedArray(),
                    showBlockedDefaultPopup = true,
                    onGranted = {
                        onGranted()
                    },
                    onDenied = {
                        onDenied()
                    },
                    onCanceledBlockedPopup = { dialog ->
                        dialog.dismiss()
                        navigateUp(force = true)
                    }
                ))
        } else {
            onGranted()
        }
    }

    override fun onLoadedList(count: Int) {
        val context = this.context ?: return
        if (count > 0) {
            mediaListForm.setEmptyView(null)
        } else {
            RecyclerViewEmptyTextForm().apply {
                createFormHolder(context, mediaListForm.getEmptyParentView())
                bindFormModel(context, "The media files are not exist or the file access permission is denied")
            }.also {
                mediaListForm.setEmptyView(it.getView())
            }
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