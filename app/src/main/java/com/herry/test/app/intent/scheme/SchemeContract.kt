package com.herry.test.app.intent.scheme

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

/**
 * Created by herry.park on 2020/06/11.
 **/
interface SchemeContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onGotoScheme(scheme: String)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun gotoScheme(url: String)
    }

    data class SchemaData (
        val title: String,
        val appLink: String,
        val dynamicLink: String = "",
        val shortLink: String = "",
    )
}