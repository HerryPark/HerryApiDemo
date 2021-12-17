package com.herry.test.app.base.mvp

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPView
import com.herry.test.rx.RxSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter<V> : MVPPresenter<V>(), LifecycleObserver {

    protected var view: V? = null
        private set

    private var _compositeDisposable: CompositeDisposable? = null

    // This property is only valid between onAttach and onDetach.
    protected val compositeDisposable get() = _compositeDisposable!!

    private var launched = false

    private var reloaded = false

    protected var lifecycleOwner: LifecycleOwner? = null
        private set

    override fun onAttach(view: V) {
        if (view is LifecycleOwner) {
            lifecycleOwner = view
            lifecycleOwner?.lifecycle?.addObserver(this)
        }
        this.view = view

        if (_compositeDisposable == null || _compositeDisposable?.isDisposed == true) {
            if (_compositeDisposable != null) {
                _compositeDisposable = null
            }
            _compositeDisposable = CompositeDisposable()
        }
    }

    override fun onDetach() {
        this.view = null
        this.lifecycleOwner = null

        compositeDisposable.dispose()
    }

    final override fun reloaded(reloaded: Boolean) {
        this.reloaded = reloaded
    }

    final override fun onLaunch() {
        this.view?.let {
            if (!launched) {
                launched = true
                onLaunch(it, false)
            } else if (reloaded) {
                reloaded = false
                onLaunch(it, true)
            } else {
                onResume(it)
            }
        }
    }

    final override fun onPause() {
        compositeDisposable.clear()
        this.view?.let {
            onPause(it)
        }
    }

    protected abstract fun onLaunch(view: V, recreated: Boolean = false)

    protected open fun onResume(view: V) {}

    protected open fun onPause(view: V) {}

    protected open fun launch(
        context: CoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job? {
        return lifecycleOwner?.lifecycleScope?.launch(context, start, block)
    }

    protected enum class LaunchWhen {
        CREATED,
        STARTED,
        RESUMED
    }

    protected open fun launch(
        launchWhen: LaunchWhen,
        block: suspend CoroutineScope.() -> Unit): Job? {
        return lifecycleOwner?.lifecycleScope?.run {
            when (launchWhen) {
                LaunchWhen.CREATED -> launchWhenCreated(block)
                LaunchWhen.STARTED -> launchWhenStarted(block)
                LaunchWhen.RESUMED -> launchWhenResumed(block)
            }
        }
    }

    protected open fun launch(function: () -> Unit) {
        // run function to Main Thread
        runOnUIThread(function)
    }

    protected fun runOnUIThread(function: () -> Unit) {
        if (view != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                function.invoke()
            } else {
                Handler(Looper.getMainLooper()).post {
                    function.invoke()
                }
            }
        }
    }

    protected fun <T> presenterObservable(
        observable: Observable<T>,
        subscribeOn: Scheduler = RxSchedulerProvider.io(),
        loadView: Boolean = false): Observable<T> {

        return observable
            .subscribeOn(subscribeOn)
            .doOnSubscribe {
                if (loadView) {
                    (view as? MVPView<*>)?.showViewLoading()
                }
            }
            .doOnError {
                (view as? MVPView<*>)?.error(it)
                if (loadView) {
                    (view as? MVPView<*>)?.hideViewLoading(false)
                }
            }
            .doOnComplete {
                if (loadView) {
                    (view as? MVPView<*>)?.hideViewLoading(true)
                }
            }
            .doOnDispose {
                if (loadView) {
                    (view as? MVPView<*>)?.hideViewLoading(true)
                }
            }
    }

    protected fun <T> subscribeObservable(
            observable: Observable<T>,
            onNext: ((T) -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null,
            onComplete: (() -> Unit)? = null,
            subscribeOn: Scheduler = RxSchedulerProvider.io(),
            observerOn: Scheduler = RxSchedulerProvider.ui(),
            loadView: Boolean = false
    ) {

        val subscribeObservable = observable
                .subscribeOn(subscribeOn)
                .observeOn(observerOn)
                .doOnSubscribe {
                    if (loadView) {
                        (view as? MVPView<*>)?.showViewLoading()
                    }
                }
                .doOnError {
                    (view as? MVPView<*>)?.error(it)
                    if (loadView) {
                        (view as? MVPView<*>)?.hideViewLoading(false)
                    }
                }
                .doOnComplete {
                    if (loadView) {
                        (view as? MVPView<*>)?.hideViewLoading(true)
                    }
                }
                .doOnDispose {
                    if (loadView) {
                        (view as? MVPView<*>)?.hideViewLoading(true)
                    }
                }

        compositeDisposable.add(
                subscribeObservable.subscribe({
                    onNext?.let { next -> next(it) }
                }, {
                    onError?.let { error -> error(it) }
                }, {
                    onComplete?.let { it() }
                })
        )
    }
}