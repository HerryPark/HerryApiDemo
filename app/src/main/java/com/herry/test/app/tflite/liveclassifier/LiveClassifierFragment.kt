package com.herry.test.app.tflite.liveclassifier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import com.herry.libs.app.activity_caller.module.ACPermission
import com.herry.libs.helper.ToastHelper
import com.herry.libs.log.Trace
import com.herry.libs.util.AppUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.launchWhenResumed
import com.herry.libs.widget.view.dialog.AppDialog
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.libs.permission.PermissionHelper
import com.herry.test.widget.TitleBarForm


class LiveClassifierFragment : BaseNavView<LiveClassifierContract.View, LiveClassifierContract.Presenter>(), LiveClassifierContract.View {
    override fun onCreatePresenter(): LiveClassifierContract.Presenter = LiveClassifierPresenter()

    override fun onCreatePresenterView(): LiveClassifierContract.View = this

    private var container: View? = null
    private var cameraPreview: PreviewView? = null

    private var blockedPermissionPopup: AppDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Trace.d("onCreateView")
        return this.container ?: inflater.inflate(R.layout.live_classifier_fragment, container, false).also { this.container = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.live_classifier_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Live Classifier", backEnable = true, backgroundColor = ViewUtil.getColor(context, android.R.color.transparent)))
        }

        this.cameraPreview = view.findViewById(R.id.live_classifier_fragment_camera_preview)
    }

    override fun onCheckPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: (() -> Unit)?, onBlocked: (() -> Unit)?) {
        if (!PermissionHelper.hasPermission(context, type)) {
            if (blockedPermissionPopup != null) {
                return
            }

            activityCaller?.call(
                ACPermission.Caller(
                    type.permissions.toTypedArray(),
                    onGranted = {
                        if (blockedPermissionPopup?.isShowing() == true) {
                            blockedPermissionPopup?.dismiss()
                        }
                    },
                    onDenied = {
                        ToastHelper.showToast(activity, "Permissions not granted by the user.")
                        navigateUp(force = true)
                    },
                    onBlocked = {
                        blockedPermissionPopup = PermissionHelper.createPermissionSettingScreenPopup(activity, onCancel = { dialog ->
                            dialog.dismiss()
                            navigateUp(force = true)
                        })?.apply {
                            this.setOnDismissListener { blockedPermissionPopup = null }
                        }
                        launchWhenResumed {
                            blockedPermissionPopup?.show()
                        }
                    })
            )
        } else {
            if (blockedPermissionPopup?.isShowing() == true) {
                blockedPermissionPopup?.dismiss()
            }
            blockedPermissionPopup = null
            onGranted()
        }
    }

    override fun getCameraPreviewView(): PreviewView? = this.getCameraPreviewView()

}