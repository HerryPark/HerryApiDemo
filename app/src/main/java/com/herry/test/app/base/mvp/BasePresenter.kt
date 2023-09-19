package com.herry.test.app.base.mvp

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.herry.libs.log.Trace
import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPPresenterLifecycle
import com.herry.libs.mvp.MVPView
import com.herry.libs.util.network.NetworkConnectionChecker
import com.herry.libs.util.network.OnNetworkConnectionChanged
import com.herry.libs.util.perform.PerformBlocks
import com.herry.libs.widget.extension.launchWhenCreated
import com.herry.libs.widget.extension.launchWhenResumed
import com.herry.libs.widget.extension.launchWhenStarted
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

    private var networkConnectionChecker: NetworkConnectionChecker? = null

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

        (view as? MVPView<*>)?.getViewContext()?.let { context: Context ->
            networkConnectionChecker = NetworkConnectionChecker(context, object : NetworkConnectionChecker.OnConnection {
                override fun onConnected() {
                    launch(LaunchWhenPresenter.LAUNCHED) {
                        (view as? OnNetworkConnectionChanged)?.onChangedNetwork(OnNetworkConnectionChanged.NetworkStatus.CONNECTED)
                        changeNetworkTo(true)
                    }
                }

                override fun onDisconnected() {
                    launch(LaunchWhenPresenter.LAUNCHED) {
                        (view as? OnNetworkConnectionChanged)?.onChangedNetwork(OnNetworkConnectionChanged.NetworkStatus.DISCONNECTED)
                        changeNetworkTo(false)
                    }
                }
            })
        }

        presenterLifecycle.setState(state = MVPPresenterLifecycle.State.ATTACHED)
    }

    override fun onDetach() {
        this.view = null
        this.viewLifecycleOwner = null

        networkConnectionChecker = null

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

    final override fun onResume() {
        val action: () -> Unit = {
            this.view?.let {
                networkConnectionChecker?.register()
                val viewLifecycleScope = viewLifecycleOwner?.lifecycleScope
                if (!launched) {
                    launched = true
                    Trace.d(TAG, "onResume (LAUNCH) [${this::class.java.simpleName}] ")
                    setResumeState(it, ResumeState.LAUNCH)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
                } else if (relaunched) {
                    relaunched = false
                    Trace.d(TAG, "onResume (RELAUNCH) [${this::class.java.simpleName}] ")
                    setResumeState(it, ResumeState.RELAUNCH)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.LAUNCHED)
                } else {
                    Trace.d(TAG, "onResume (RESUME) [${this::class.java.simpleName}] ")
                    setResumeState(it, ResumeState.RESUME)
                    presenterLifecycle.setState(viewLifecycleScope = viewLifecycleScope, state = MVPPresenterLifecycle.State.RESUMED)
                }
            }
        }

        launchWhenAfterTransition?.let {
            action()
        } ?: action()
    }

    private var currentResumeState: ResumeState? = null

    private fun setResumeState(view: V, resumeState: ResumeState) {
        currentResumeState = resumeState
        onResume(view, resumeState)
    }

    protected fun getResumeState(): ResumeState? = currentResumeState

    final override fun onPause() {
        networkConnectionChecker?.unregister()
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
        return viewLifecycleOwner?.run {
            when (launchWhen) {
                LaunchWhenView.CREATED -> this.launchWhenCreated(block = block)
                LaunchWhenView.STARTED -> this.launchWhenStarted(block = block)
                LaunchWhenView.RESUMED -> this.launchWhenResumed(block = block)
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

    protected fun clearPendingLaunch() {
        presenterLifecycle.clearPendingStateBlocks()
    }

    private val postOnUIThreadHandler = Handler(Looper.getMainLooper())

    protected fun postOnUIThread(delayMs: Long = 0L, function: () -> Unit) {
        if (view != null) {
            postOnUIThreadHandler.postDelayed({
                function.invoke()
            }, delayMs)
        }
    }

    protected fun cancelOnUIThreadPosting() {
        postOnUIThreadHandler.removeCallbacksAndMessages(null)
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

    @Suppress("SameParameterValue")
    protected fun createPerformBlocks(
        launchWhen: LaunchWhenPresenter = LaunchWhenPresenter.LAUNCHED
    ): PerformBlocks = object : PerformBlocks() {
        override fun performActionsJob(block: suspend CoroutineScope.() -> Unit): Job? {
            return launch(launchWhen) { block() }
        }

        override fun actionJob(block: suspend CoroutineScope.() -> Unit): Job? {
            return launch(launchWhen) { block() }
        }
    }


    private fun changeNetworkTo(on: Boolean) {
        val view = this.view ?: return

        onChangedNetworkConnection(view, on)
    }

    protected open fun onChangedNetworkConnection(view: V, on: Boolean) {}

    protected fun isConnectedNetwork(): Boolean = networkConnectionChecker?.isConnected() ?: false
}