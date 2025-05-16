package com.herry.test.app.tflite.digitclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.herry.libs.util.tuple.Tuple1
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.atomic.AtomicReference

class DigitClassifierPresenter : DigitClassifierContract.Presenter() {
    private var loadedImage: AtomicReference<Any?> = AtomicReference()
    private var digitImageClassifier: DigitImageClassifier? = null

    override fun onDetach() {
        digitImageClassifier?.release()

        super.onDetach()
    }

    override fun onResume(view: DigitClassifierContract.View, state: ResumeState) {
        displayLoadedImage(loadedImage.get())
    }

    override fun loadedImage(loadedImage: Any?, onLoaded: ((image: Any?) -> Unit)?) {
        launch(Dispatchers.IO) {
            val image = if (loadedImage is ByteArray && loadedImage.size > 0) {
                BitmapFactory.decodeByteArray(loadedImage, 0, loadedImage.size)
            } else loadedImage

            this@DigitClassifierPresenter.loadedImage.set(image)

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
                val classified = (digitImageClassifier ?: DigitImageClassifier().also { digitImageClassifier = it }).classify(context, image)
                Tuple1(classified)
            },
            onError = {
                view?.onError()
            },
            onNext = {
                val classified = it.t1
                view?.onClassified(classified.number.toString(), classified.accuracy)
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