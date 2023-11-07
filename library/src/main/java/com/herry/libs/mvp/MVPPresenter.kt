package com.herry.libs.mvp

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel

@Suppress("unused")
abstract class MVPPresenter<in V>: ViewModel() {
    protected val presenterLifecycle = MVPPresenterLifecycle()

    @MainThread
    abstract fun onAttach(view: V)

    @MainThread
    abstract fun onDetach()

    @MainThread
    open fun onStart() {}

    @MainThread
    abstract fun onResume()

    @MainThread
    abstract fun onPause()

    @MainThread
    open fun onStop() {}

    abstract fun relaunched(recreated: Boolean)
}