package com.herry.test.app.pick

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.app.activity_caller.module.ACPick
import com.herry.libs.app.activity_caller.module.ACTake
import com.herry.libs.helper.ToastHelper
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.libs.permission.PermissionHelper
import com.herry.test.widget.TitleBarForm
import java.io.IOException

class PickListFragment: BaseNavView<PickListContract.View, PickListContract.Presenter>(), PickListContract.View {

    override fun onCreatePresenter(): PickListContract.Presenter = PickListPresenter()

    override fun onCreatePresenterView(): PickListContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.pick_list_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.pick_list_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Pick List", backEnable = true))
        }

        view.findViewById<RecyclerView>(R.id.pick_list_fragment_list)?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@PickListFragment.adapter
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    override fun onScreen(type: PickListContract.PickType) {
        when (type) {
            PickListContract.PickType.PICK_PHOTO -> {
                activityCaller?.call(ACPick.PickImageOnly { result ->
                    if (result.success) {
                        val picked: Uri? = result.uris.firstOrNull()
                        Trace.d("selected photo counts: ${result.uris.size}")
                        ToastHelper.showToast(activity, "selected photo: ${picked.toString()}")
                    } else {
                        ToastHelper.showToast(activity, "cancel photo selection")
                    }
                })
            }
            PickListContract.PickType.PICK_VIDEO -> {
                activityCaller?.call(ACPick.PickVideoOnly { result ->
                    if (result.success) {
                        val picked: Uri? = result.uris.firstOrNull()
                        Trace.d("selected video counts: ${result.uris.size}")
                        ToastHelper.showToast(activity, "selected video: ${picked.toString()}")
                    } else {
                        ToastHelper.showToast(activity, "cancel video selection")
                    }
                })
            }
            PickListContract.PickType.PICK_PHOTO_AND_VIDEO -> {
                activityCaller?.call(ACPick.PickVisualMedia { result ->
                    if (result.success) {
                        val picked: Uri? = result.uris.firstOrNull()
                        Trace.d("selected photo and video counts: ${result.uris.size}")
                        ToastHelper.showToast(activity, "selected photo and video: ${picked.toString()}")
                    } else {
                        ToastHelper.showToast(activity, "cancel photo and video selection")
                    }
                })
            }
            PickListContract.PickType.TAKE_PHOTO -> {
                val onTake: () -> Unit = {
                    val tempFile = try {
                        presenter?.getToTakeTempFile(type)
                    } catch (ex: IOException) {
                        null
                    }

                    // Create the File where the photo should go
                    val saveFileURI: Uri? = presenter?.getUriForFileProvider(tempFile)
                    if (tempFile != null && saveFileURI != null) {
                        activityCaller?.call(ACTake.TakeImage(saveFileURI) { result ->
                            presenter?.picked(tempFile = tempFile, picked = result.uri, type = type, result.success)
                        })
                    }
                }

                val permissionType = PermissionHelper.Type.CAMERA
                if (!PermissionHelper.hasPermission(context, permissionType)) {
                    activityCaller?.call(
                        ACPermission.Caller(
                            permissionType.permissions.toTypedArray(),
                            onGranted = {
                                onTake()
                            }
                        ))
                } else {
                    onTake()
                }
            }

            PickListContract.PickType.TAKE_VIDEO -> {
                val onTake: () -> Unit = {
                    val tempFile = try {
                        presenter?.getToTakeTempFile(type)
                    } catch (ex: IOException) {
                        null
                    }

                    // Create the File where the video should go
                    val saveFileURI: Uri? = presenter?.getUriForFileProvider(tempFile)
                    if (tempFile != null && saveFileURI != null) {
                        activityCaller?.call(ACTake.TakeVideo(saveFileURI) { result ->
                            presenter?.picked(tempFile = tempFile, picked = result.uri, type = type, result.success)
                        })
                    }
                }

                val permissionType = PermissionHelper.Type.CAMCORDER
                if (!PermissionHelper.hasPermission(context, permissionType)) {
                    activityCaller?.call(
                        ACPermission.Caller(
                            permissionType.permissions.toTypedArray(),
                            onGranted = {
                                onTake()
                            }
                        ))
                } else {
                    onTake()
                }
            }
        }
    }

    override fun onPicked(message: String) {
        ToastHelper.showToast(activity, message)
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(PickItemForm())
        }
    }

    private inner class PickItemForm : NodeForm<PickItemForm.Holder, PickListContract.PickType>(
        Holder::class, PickListContract.PickType::class
    ) {
        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val title: TextView? = view.findViewById(R.id.main_test_item_title)
            init {
                view.setOnClickListener {
                    NodeRecyclerForm.getBindModel(this@PickItemForm, this@Holder)?.let {
                        presenter?.pick(it)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.main_test_item


        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: PickListContract.PickType) {
            holder.title?.text = when (model) {
                PickListContract.PickType.PICK_PHOTO -> "Pick Photo"
                PickListContract.PickType.PICK_VIDEO -> "Pick Video"
                PickListContract.PickType.PICK_PHOTO_AND_VIDEO -> "Pick Photo And Video"
                PickListContract.PickType.TAKE_PHOTO -> "Take Photo"
                PickListContract.PickType.TAKE_VIDEO -> "Take Movie"
            }
        }

    }
}