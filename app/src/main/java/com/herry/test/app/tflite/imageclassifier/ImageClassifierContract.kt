package com.herry.test.app.tflite.imageclassifier

import android.graphics.Bitmap
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface ImageClassifierContract {

    interface View : MVPView<Presenter> {
        fun onLoadedImage(loadedImage: Any?)
        fun onClassified(result: String, accuracy: Float)
        fun onError()
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun loadedImage(loadedImage: Any?, onLoaded: ((image: Any?) -> Unit)? = null)
        abstract fun classify(image: Bitmap)
        abstract fun clearClassify()
    }
}