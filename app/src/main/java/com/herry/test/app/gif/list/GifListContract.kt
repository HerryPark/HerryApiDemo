package com.herry.test.app.gif.list

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter
import com.herry.libs.permission.PermissionHelper
import com.herry.test.data.GifMediaFileInfoData

/**
 * Created by herry.park on 2020/06/11.
 **/
interface GifListContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onCheckPermission(type: PermissionHelper.Type, onGranted: () -> Unit, onDenied: () -> Unit)
        fun onLoadedList(count: Int)
        fun onDetail(content: GifMediaFileInfoData)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun decode(content: GifMediaFileInfoData)
    }

}