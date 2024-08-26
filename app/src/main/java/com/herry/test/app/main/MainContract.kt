package com.herry.test.app.main

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

/**
 * Created by herry.park on 2020/06/11.
 **/
interface MainContract {

    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: TestItemType)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun moveToScreen(type: TestItemType)
    }

    enum class TestItemType(val label: String) {
        SCHEME_TEST ("Intent"),
        GIF_DECODER ("GIF Decoder"),
        CHECKER_LIST ("Data Checker"),
        LAYOUT_SAMPLE ("Layout Sample"),
        PICK ("Pick"),
        NESTED_FRAGMENTS ("Nested Fragments"),
        NESTED_BOTTOM_NAV_FRAGMENTS ("Nested Bottom Navigator Fragments"),
        DIALOGS ("Dialogs"),
        LIST ("List"),
        SKELETON ("Skeleton"),
        RESIZING_UI ("Resizing UI"),
        SAMPLE_APP ("Sample Application"),
        PAINTER ("Painter"),
        TENSOR_FLOW_LITE ("Tensorflow-lite"),
        WIDGETS ("Widgets"),
        TIMELINE ("Timeline"),
        DOWNLOADABLE_FONTS ("Downloadable fonts"),
        MVVM_TEST ("MVVM test")
    }
}
