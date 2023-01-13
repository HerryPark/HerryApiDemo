package com.herry.test.app.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter

/**
 * Created by herry.park on 2020/06/11.
 **/
interface MainContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: TestItemType)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun moveToScreen(type: TestItemType)
    }

    enum class TestItemType {
        SCHEME_TEST,
        GIF_DECODER,
        CHECKER_LIST,
        LAYOUT_SAMPLE,
        PICK,
        NESTED_FRAGMENTS,
        NESTED_BOTTOM_NAV_FRAGMENTS,
        APP_DIALOG,
        LIST,
        SKELETON,
        RESIZING_UI,
        SAMPLE_APP,
        PAINTER,
        TENSOR_FLOW_LITE
    }
}