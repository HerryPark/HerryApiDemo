package com.herry.test.app.tflite.digitclassifier

import android.graphics.Bitmap
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BasePresenter

interface DigitClassifierContract {

    interface View : MVPView<Presenter> {
        fun onLoadedImage(loadedImage: Any?)
        fun onClassified(result: String, accuracy: Float)
        fun onError()
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun loadedImage(loadedImage: Any?)
        abstract fun classify(image: Bitmap)
        abstract fun clear()
    }
}