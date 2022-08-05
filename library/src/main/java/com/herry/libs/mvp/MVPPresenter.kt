package com.herry.libs.mvp

import androidx.lifecycle.ViewModel

@Suppress("unused")
abstract class MVPPresenter<in V>: ViewModel() {
    protected val presenterLifecycle = MVPPresenterLifecycle()

    abstract fun onAttach(view: V)

    abstract fun onDetach()

    abstract fun onLaunch()

    abstract fun onPause()

    abstract fun relaunched(recreated: Boolean)
}