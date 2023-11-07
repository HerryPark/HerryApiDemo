package com.herry.test.app.intent.share

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter
import com.herry.libs.permission.PermissionHelper
import com.herry.test.widget.MediaSelectionForm

/**
 * Created by herry.park on 2020/06/11.
 **/
interface ShareMediaListContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onRequestPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: () -> Unit, onBlocked: () -> Unit)
        fun onUpdatedMediaPermission(model: MediaSelectionForm.Model)
        fun onLoadedList(count: Int, permitted: PermissionHelper.Permitted)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun setMediaType(type: MediaSelectionForm.MediaType)
    }

}