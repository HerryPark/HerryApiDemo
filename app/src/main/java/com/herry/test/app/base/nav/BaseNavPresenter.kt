package com.herry.test.app.base.nav

import android.os.Handler
import android.os.Looper
import com.herry.test.app.base.mvp.BasePresenter

abstract class BaseNavPresenter<V>: BasePresenter<V>() {

    fun navTransitionStart() {
        super.startTransition()
    }

    open fun navTransitionEnd() {
        super.endTransition()
    }
}