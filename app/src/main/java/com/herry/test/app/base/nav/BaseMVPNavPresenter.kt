package com.herry.test.app.base.nav

import com.herry.test.app.base.mvp.BaseMVPPresenter

abstract class BaseMVPNavPresenter<V>: BaseMVPPresenter<V>() {

    fun navTransitionStart() {
        super.startTransition()
    }

    open fun navTransitionEnd() {
        super.endTransition()
    }
}