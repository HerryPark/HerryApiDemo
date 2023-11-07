package com.herry.test.app.dialog.list

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter

interface DialogListContract {
    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: ItemType)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun moveToScreen(type: ItemType)
    }

    enum class ItemType {
        APP_DIALOGS,
        BOTTOM_SHEET_LIST_DIALOG,
        BOTTOM_SHEET_SCREEN_DIALOG,
        FULL_SCREEN_DIALOG
    }
}