package com.herry.test.app.base.mvp

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.herry.libs.log.Trace
import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPPresenterLifecycle
import com.herry.libs.mvp.MVPView
import com.herry.test.rx.LastOneObservable
import com.herry.test.rx.RxSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter<V> : MVPPresenter<V>(), LifecycleObserver {

    protected var view: V? = null
        private set

    private var _compositeDisposable: CompositeDisposable? = null

    // This property is only valid between onAttach and onDetach.
    protected val compositeDisposable get() = _compositeDisposable!!

    private var lastOnObservables: MutableSet<LastOneObservable<*>> = mutableSetOf()

    private var launched = false

    private var relaunched = false

    protected var viewLifecycleOwner: LifecycleOwner? = null
        private set

    override fun onAttach(view: V) {
        if (view is LifecycleOwner) {
            viewLifecycleOwner = view
            viewLifecycleOwner?.lifecycle?.addObserver(this)
        }
        this.view = view

        if (_compositeDisposable == null || _compositeDisposable?.isDisposed == true) {
            if (_compositeDisposable != null) {
                _compositeDisposable = null
            }
            _compositeDisposable = CompositeDisposable()
        }

        presenterLifecycle.setState(state = MVPPresenterLifecycle.State.ATTACHED)
    }

    override fun onDetach() {
        this.view = null
        this.viewLifecycleOwner = null

        compositeDisposable.dispose()

        lastOnObservables.forEach {
            it.dispose()
        }
        lastOnObservables.clear()

        presenterLifecycle.setState(state = MVPPresenterLifecycle.State.CREATED)
    }

    final override fun relaunched(recreated: Boolean) {
        this.relaunched = recreated
    }

    final override fun onLaunch() {
        Trace.d("Herry", "onLaunch() = View.${viewLifecycleOwner?.lifecycle?.currentState}")
        this.view?.let {
            Trace.d("Herry", "onLaunch() = Presenter.${getCurrentPresenterState()} launched = $launched relaunched = $relaunched")
            val lifecycleScope = viewLifecycleOwner?.lifecycleScope
            if (!launched) {
                launched = true
                onLaunch(it, false)
                presenterLifecycle.setState(lifecycleScope = lifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
            } else if (relaunched) {
                relaunched = false
                onLaunch(it, true)
                presenterLifecycle.setState(lifecycleScope = lifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
            } else {
                onResume(it)
                presenterLifecycle.setState(lifecycleScope = lifecycleScope, state = MVPPresenterLifecycle.State.RESUMED)
            }
        }
    }

    final override fun onPause() {
        compositeDisposable.clear()
        this.view?.let {
            onPause(it)
            presenterLifecycle.setState(state = MVPPresenterLifecycle.State.ATTACHED)
        }
    }

    fun isLaunched(): Boolean = launched && !relaunched

    protected abstract fun onLaunch(view: V, recreated: Boolean = false)

    protected open fun onResume(view: V) {}

    protected open fun onPause(view: V) {}

    protected open fun launch(
        context: CoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job? {
        return viewLifecycleOwner?.lifecycleScope?.launch(context, start, block)
    }

    protected enum class LaunchWhenView {
        CREATED,
        STARTED,
        RESUMED
    }

    protected open fun launch(
        launchWhen: LaunchWhenView,
        block: suspend CoroutineScope.() -> Unit): Job? {
        return viewLifecycleOwner?.lifecycleScope?.run {
            when (launchWhen) {
                LaunchWhenView.CREATED -> launchWhenCreated(block)
                LaunchWhenView.STARTED -> launchWhenStarted(block)
                LaunchWhenView.RESUMED -> launchWhenResumed(block)
            }
        }
    }

    protected fun getCurrentPresenterState(): MVPPresenterLifecycle.State = presenterLifecycle.getCurrentState()

    protected enum class LaunchWhenPresenter {
        LAUNCHED,
        RESUMED;
    }

    protected open fun launch(
        launchWhen: LaunchWhenPresenter,
        block: suspend CoroutineScope.() -> Unit
    ): Job? {
        return viewLifecycleOwner?.lifecycleScope?.run {
            when (launchWhen) {
                LaunchWhenPresenter.LAUNCHED -> presenterLifecycle.launchWhenPresenterLaunched(this, block)
                LaunchWhenPresenter.RESUMED -> presenterLifecycle.launchWhenPresenterResumed(this, block)
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
        observerOn: Scheduler = RxSchedulerProvider.ui(),
        loadView: Boolean = false
    ): Observable<T> {

        return observable
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
        compositeDisposable.add(
            presenterObservable(
                observable,
                subscribeOn,
                observerOn,
                loadView
            ).subscribe({
                onNext?.let { next -> next(it) }
            }, {
                onError?.let { error -> error(it) }
            }, {
                onComplete?.let { it() }
            })
        )
    }

    protected fun <T> subscribeLastOneObservable(
        lastOneObservable: LastOneObservable<T>,
        observable: Observable<T>,
        subscribeOn: Scheduler = RxSchedulerProvider.io(),
        observerOn: Scheduler = RxSchedulerProvider.ui(),
        loadView: Boolean = false
    ) {
        lastOnObservables.add(lastOneObservable)

        if (!lastOneObservable.isDisposed()) {
            lastOneObservable.dispose()
        }

        lastOneObservable.subscribe(
            presenterObservable(
                observable = observable,
                subscribeOn = subscribeOn,
                observerOn = observerOn,
                loadView = loadView
            )
        )
    }
}