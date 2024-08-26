package com.herry.test.app.resizing

import androidx.annotation.DrawableRes
import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface ResizingUIContract {
    interface View: MVPView<Presenter>, INodeRoot {
    }

    abstract class Presenter: BaseMVPPresenter<View>() {
    }

    data class MenuItemModel(@DrawableRes val icon: Int)
}