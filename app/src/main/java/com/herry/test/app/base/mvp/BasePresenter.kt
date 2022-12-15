package com.herry.test.app.base.mvp

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
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

abstract class BasePresenter<V> : MVPPresenter<V>(), LifecycleObserver {

    companion object {
        private const val TAG = "BasePresenter"
    }

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

    enum class ResumeState {
        LAUNCH,
        RELAUNCH,
        RESUME;

        fun isLaunch() = this == LAUNCH || this == RELAUNCH
    }

    final override fun onLaunch() {
        val action: () -> Unit = {
            this.view?.let {
                val viewLifecycleScope = viewLifecycleOwner?.lifecycleScope
                if (!launched) {
                    launched = true
                    Trace.d(TAG, "onResume (LAUNCH) [${this::class.java.simpleName}] ")
                    onResume(it, ResumeState.LAUNCH)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
                } else if (relaunched) {
                    relaunched = false
                    Trace.d(TAG, "onResume (RELAUNCH) [${this::class.java.simpleName}] ")
                    onResume(it, ResumeState.RELAUNCH)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
                } else {
                    Trace.d(TAG, "onResume (RESUME) [${this::class.java.simpleName}] ")
                    onResume(it, ResumeState.RESUME)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.RESUMED)
                }
            }
        }

        launchWhenAfterTransition?.let {
            action()
        } ?: action()
    }

    final override fun onPause() {
        compositeDisposable.clear()
        this.view?.let {
            onPause(it)
            presenterLifecycle.setState(state = MVPPresenterLifecycle.State.ATTACHED)
        }
    }

    fun isLaunched(): Boolean = launched && !relaunched

    protected abstract fun onResume(view: V, state: ResumeState)

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

    private var launchWhenAfterTransition: ConcurrentLinkedQueue<suspend CoroutineScope.() -> Unit>? = null

    protected fun startTransition() {
        launchWhenAfterTransition = ConcurrentLinkedQueue()
    }

    protected fun endTransition() {
        launch(Dispatchers.Main.immediate) {
            launchWhenAfterTransition?.let { blocks ->
                val iterator = blocks.iterator()
                while (iterator.hasNext()) {
                    iterator.next().also { iterator.remove() }.invoke(this/*CoroutineScope*/)
                }

                blocks.clear()
            }?.also {
                launchWhenAfterTransition = null
            }
        }
    }

    protected interface OnLoadingListener {
        fun onStarted()
        fun onStopped()
        fun onError()
    }

    protected fun <T> presenterObservable(
        observable: Observable<T>,
        subscribeOn: Scheduler = RxSchedulerProvider.io(),
        observerOn: Scheduler = RxSchedulerProvider.ui(),
        loadView: Boolean = false,
        onLoading: OnLoadingListener? = null
    ): Observable<T> {

        return observable
            .subscribeOn(subscribeOn)
            .observeOn(observerOn)
            .doOnSubscribe {
                onLoading?.onStarted()
                if (loadView) {
                    (view as? MVPView<*>)?.showViewLoading()
                }
            }
            .doOnError {
                onLoading?.onError()
                (view as? MVPView<*>)?.error(it)
                if (loadView) {
                    (view as? MVPView<*>)?.hideViewLoading(false)
                }
            }
            .doOnComplete {
                onLoading?.onStopped()
                if (loadView) {
                    (view as? MVPView<*>)?.hideViewLoading(true)
                }
            }
            .doOnDispose {
                onLoading?.onStopped()
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
        loadView: Boolean = false,
        onLoading: OnLoadingListener? = null
    ) {
        compositeDisposable.add(
            presenterObservable(
                observable,
                subscribeOn,
                observerOn,
                loadView,
                onLoading
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
        loadView: Boolean = false,
        onLoading: OnLoadingListener? = null
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
                loadView = loadView,
                onLoading = onLoading
            )
        )
    }
}