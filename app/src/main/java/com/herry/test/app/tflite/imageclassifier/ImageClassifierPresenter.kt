package com.herry.test.app.tflite.imageclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.herry.libs.util.tuple.Tuple1
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.atomic.AtomicReference

class ImageClassifierPresenter : ImageClassifierContract.Presenter() {
    private var loadedImage: AtomicReference<Any?> = AtomicReference()
    private var imageClassifier: ImageClassifier? = null

    override fun onDetach() {
        imageClassifier?.release()

        super.onDetach()
    }

    override fun onResume(view: ImageClassifierContract.View, state: ResumeState) {
        displayLoadedImage(loadedImage.get())
    }

    override fun loadedImage(loadedImage: Any?, onLoaded: ((image: Any?) -> Unit)?) {
        launch(Dispatchers.IO) {
            val image = if (loadedImage is ByteArray && loadedImage.size > 0) {
                BitmapFactory.decodeByteArray(loadedImage, 0, loadedImage.size)
            } else loadedImage

            this@ImageClassifierPresenter.loadedImage.set(image)

            launch(Dispatchers.Main) {
                onLoaded?.invoke(image)
            }
        }
    }

    private fun displayLoadedImage(loadedImage: Any?) {
        launch(LaunchWhenPresenter.LAUNCHED) {
            view?.onLoadedImage(loadedImage)
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
                val classified = it.t1
                view?.onClassified(classified.what, classified.accuracy)
            },
            loadView = true
        )
    }

    override fun clearClassify() {
        loadedImage(null) { image ->
            displayLoadedImage(image)
        }
    }
}