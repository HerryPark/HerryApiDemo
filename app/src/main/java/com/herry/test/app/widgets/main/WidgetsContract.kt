package com.herry.test.app.widgets.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface WidgetsContract {

    interface View : MVPView<Presenter>, INodeRoot {

    }

    abstract class Presenter : BaseMVPPresenter<View>() {

    }

    enum class Widget {
        SPINNERS,
        APP_BUTTONS,
        TITLE_FORM
    }
}