package com.herry.test.app.pick

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.app.activity_caller.module.ACTake
import com.herry.libs.helper.ToastHelper
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.launchWhenResumed
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
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
                val intent = Intent(Intent.ACTION_PICK).apply {
                    setType(MediaStore.Images.Media.CONTENT_TYPE)
                    data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                intent.resolveActivity(requireActivity().packageManager ?: return) ?: return

                activityCaller?.call(
                    ACPermission.Caller(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        onGranted = {
                            activityCaller?.call(ACTake.PickPicture { result ->
                                if (result.success) {
                                    val picked: Uri? = result.uris.firstOrNull()
                                    ToastHelper.showToast(activity, "selected photo: ${picked.toString()}")
                                } else {
                                    ToastHelper.showToast(activity, "cancel photo selection")
                                }
                            })
                        }
                    ))
            }
            PickListContract.PickType.PICK_MOVIE -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    setType(MediaStore.Video.Media.CONTENT_TYPE)
                    data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                intent.resolveActivity(requireActivity().packageManager ?: return) ?: return

                activityCaller?.call(
                    ACPermission.Caller(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        onGranted = {
                            activityCaller?.call(ACTake.PickVideo { result ->
                                if (result.success) {
                                    val picked: Uri? = result.uris.firstOrNull()
                                    ToastHelper.showToast(activity, "selected video: ${picked.toString()}")
                                } else {
                                    ToastHelper.showToast(activity, "cancel video selection")
                                }
                            })
                        }
                    ))
            }
            PickListContract.PickType.TAKE_PHOTO,
            PickListContract.PickType.TAKE_MOVIE -> {
                activityCaller?.call(
                    ACPermission.Caller(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        onGranted = {
                            val tempFile = try {
                                presenter?.getToTakeTempFile(type)
                            } catch (ex: IOException) {
                                null
                            } ?: return@Caller

                            // Create the File where the photo should go
                            val saveFileURI: Uri = presenter?.getUriForFileProvider(tempFile) ?: return@Caller

                            if (type == PickListContract.PickType.TAKE_PHOTO) {
                                activityCaller?.call(ACTake.TakePicture(saveFileURI) { result ->
                                    val activity = result.callActivity
                                    activity.launchWhenResumed {
                                        presenter?.picked(tempFile = tempFile, picked = result.uris.firstOrNull(), type = type, result.success)
                                    }
                                })
                            } else {
                                activityCaller?.call(ACTake.TakeVideo(saveFileURI) { result ->
                                    val activity = result.callActivity
                                    Trace.d("call return: Activity.${activity.lifecycle.currentState}")
                                    Trace.d("call return: View.${lifecycle.currentState}")
                                    activity.launchWhenResumed {
                                        presenter?.picked(tempFile = tempFile, picked = result.uris.firstOrNull(), type = type, result.success)
                                    }
                                })
                            }
                        }
                    ))
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
                PickListContract.PickType.PICK_MOVIE -> "Pick Movie"
                PickListContract.PickType.TAKE_PHOTO -> "Take Photo"
                PickListContract.PickType.TAKE_MOVIE -> "Take Movie"
            }
        }

    }
}