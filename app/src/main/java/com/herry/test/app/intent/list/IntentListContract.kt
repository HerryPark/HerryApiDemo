package com.herry.test.app.intent.list

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

/**
 * Created by herry.park on 2020/06/11.
 **/
interface IntentListContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: TestItemType)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun moveToScreen(type: TestItemType)
    }

    enum class TestItemType(val label: String) {
        SCHEME_TEST ("Scheme Intent"),
        MEDIA_SHARE_TEST ("Media Share Intent")
    }
}