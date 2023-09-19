package com.herry.test.app.widgets.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter

interface WidgetsContract {

    interface View : MVPView<Presenter>, INodeRoot {

    }

    abstract class Presenter : BasePresenter<View>() {

    }

    enum class Widget {
        SPINNERS,
    }
}