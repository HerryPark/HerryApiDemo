package com.herry.test.app.list.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface ListContract {
    interface View : MVPView<Presenter>, INodeRoot

    abstract class Presenter : BaseMVPPresenter<View>()

    enum class Type {
        FAST_SCROLLER,
        ENDLESS,
        INDEXER,
        INFINITE_LINEAR_HORIZONTAL
    }
}