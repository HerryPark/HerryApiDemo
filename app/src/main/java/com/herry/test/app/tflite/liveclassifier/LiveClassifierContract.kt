package com.herry.test.app.tflite.liveclassifier

import androidx.camera.view.PreviewView
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter
import com.herry.libs.permission.PermissionHelper

interface LiveClassifierContract {

    interface View : MVPView<Presenter> {
        fun onCheckPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: (() -> Unit)? = null, onBlocked: (() -> Unit)? = null)
        fun getCameraPreviewView(): PreviewView?
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
    }
}