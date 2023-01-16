package com.herry.test.app.tflite.imageclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.herry.libs.util.tuple.Tuple1
import io.reactivex.Observable

class ImageClassifierPresenter : ImageClassifierContract.Presenter() {
    private var loadedImage: Any? = null
    private var imageClassifier: ImageClassifier? = null

    override fun onDetach() {
        imageClassifier?.release()

        super.onDetach()
    }

    override fun onResume(view: ImageClassifierContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            loadedImage(loadedImage)
        }
    }

    override fun loadedImage(loadedImage: Any?) {
        launch(LaunchWhenPresenter.LAUNCHED) {
            val image = if (loadedImage is ByteArray && loadedImage.size > 0) {
                BitmapFactory.decodeByteArray(loadedImage, 0, loadedImage.size)
            } else loadedImage

            this@ImageClassifierPresenter.loadedImage = image
            view?.onLoadedImage(image)
        }
    }

    override fun classify(image: Bitmap) {
        val context = view?.getViewContext() ?: return
        subscribeObservable(
            observable = Observable.fromCallable {
                val classified = (imageClassifier ?: ImageClassifier().also { imageClassifier = it }).classify(context, image)
                Tuple1(classified)
            },
            onError = {
                view?.onError()
            },
            onNext = {
                val classified = it.t1 ?: return@subscribeObservable
                view?.onClassified(classified.what, classified.accuracy)
            },
            loadView = true
        )
    }

    override fun clear() {
        loadedImage(null)
    }
}