package com.herry.test.app.tflite.list

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface TFLiteListContract {

    interface View : MVPView<Presenter>, INodeRoot

    abstract class Presenter : BaseMVPPresenter<View>()

    enum class Item {
        DIGIT_CLASSIFIER,
        IMAGE_CLASSIFIER,
        LIVE_CLASSIFIER
    }
}