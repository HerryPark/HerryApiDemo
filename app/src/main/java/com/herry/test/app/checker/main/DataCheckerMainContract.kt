package com.herry.test.app.checker.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

/**
 * Created by herry.park on 2020/7/7
 **/
interface DataCheckerMainContract {

    interface View : MVPView<Presenter>, INodeRoot

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun refresh()
    }

    data class PasswordModel(
        val password: String
    )
}