package com.herry.test.app.intent.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerViewEmptyTextForm
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerViewForm
import com.herry.test.BuildConfig
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.libs.permission.PermissionHelper
import com.herry.test.data.MediaFileInfoData
import com.herry.test.widget.MediaAccessRequestForm
import com.herry.test.widget.MediaSelectionForm
import com.herry.test.widget.Popup
import com.herry.test.widget.TitleBarForm
import java.io.File

/**
 * Created by herry.park on 2020/06/11.
 **/
class ShareMediaListFragment : BaseNavView<ShareMediaListContract.View, ShareMediaListContract.Presenter>(), ShareMediaListContract.View {

    override fun onCreatePresenter(): ShareMediaListContract.Presenter = ShareMediaListPresenter()

    override fun onCreatePresenterView(): ShareMediaListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    private var mediaSelection = MediaSelectionForm(
        onClickMediaType = { type ->
            presenter?.setMediaType(type)
        }
    )

    private var mediaListForm = object: RecyclerViewForm() {
        override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView) {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
                if (itemAnimator is SimpleItemAnimator) {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                adapter = this@ShareMediaListFragment.adapter
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.share_media_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.share_media_list_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Share Media List", backEnable = true))
        }

        view.findViewById<View?>(R.id.share_media_list_fragment_list)?.let {
            mediaListForm.bindHolder(context, it)
        }

        view.findViewById<View?>(R.id.share_media_list_fragment_media_selection)?.let {
            mediaSelection.bindHolder(context, it)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FileListItemForm { data ->
                Popup(requireActivity()).apply {
                    setMessage("path: ${data.path}\n" +
                            "mimetype: ${data.mimeType}")
                    setNegativeButton("Cancel")
                    setPositiveButton("View") { dialog, _ ->
                        dialog.dismiss()
                        actionView(data)
                    }
                }.show()
            })
        }
    }

    override fun onRequestPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: () -> Unit, onBlocked: () -> Unit) {
        activityCaller?.call(ACPermission.Caller(
            permissions = type.permissions.toTypedArray(),
            showBlockedDefaultPopup = false,
            onGranted = {
                onGranted()
            },
            onDenied = {
                onDenied()
            },
            onBlocked = {
                onBlocked()
            }
        ))
    }

    override fun onUpdatedMediaPermission(model: MediaSelectionForm.Model) {
        val context = context ?: return
        mediaSelection.bindFormModel(context, model)
    }

    override fun onLoadedList(count: Int, permitted: PermissionHelper.Permitted) {
        val context = this.context ?: return
        if (count > 0) {
            mediaListForm.setEmptyView(null)
        } else {
            when (permitted) {
                PermissionHelper.Permitted.BLOCKED -> {
                    MediaAccessRequestForm(onGotoSettings = {
                        AppUtil.showAppInfoSettingScreen(context)
                    }).apply {
                        createFormHolder(context, mediaListForm.getEmptyParentView())
                        bindFormModel(context, MediaAccessRequestForm.Model())
                    }.also {
                        mediaListForm.setEmptyView(it.getView())
                    }
                }
                PermissionHelper.Permitted.GRANTED,
                PermissionHelper.Permitted.DENIED -> {
                    RecyclerViewEmptyTextForm().apply {
                        createFormHolder(context, mediaListForm.getEmptyParentView())
                        bindFormModel(context, "The media files are not exist or the file access permission is denied")
                    }.also {
                        mediaListForm.setEmptyView(it.getView())
                    }
                }
            }
        }
    }

    private fun actionView(content: MediaFileInfoData) {
        activityCaller?.call(
            ACNavigation.IntentCaller(
                Intent().apply {
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(content.path)
                    )
                    action = Intent.ACTION_VIEW
                    setDataAndType(uri, content.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            ))
    }

    private fun actionShare(content: MediaFileInfoData) {
        activityCaller?.call(
            ACNavigation.IntentCaller(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, Uri.parse(content.path))
                type = content.mimeType
            }
        ))
    }

    private inner class FileListItemForm(private val onInformation: ((data: MediaFileInfoData) -> Unit)? = null) : NodeForm<FileListItemForm.Holder, MediaFileInfoData>(Holder::class, MediaFileInfoData::class) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val name: TextView? = view.findViewById(R.id.file_list_item_name)
            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@FileListItemForm, this@Holder)?.let {
                        actionShare(it)
                    }
                }
                view.setOnLongClickListener {
                    NodeRecyclerForm.getBindModel(this@FileListItemForm, this@Holder)?.let {
                        onInformation?.invoke(it)
                    }
                    true
                }
            }
        }

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onLayout(): Int = R.layout.file_list_item

        override fun onBindModel(context: Context, holder: Holder, model: MediaFileInfoData) {
            holder.name?.text = model.name
        }
    }
}