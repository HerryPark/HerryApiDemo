package com.herry.test.app.sample.home

import android.os.Bundle
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.nav.BaseMVPNavPresenter

interface HomeContract {
    interface View : MVPView<Presenter> {
        fun onSelectTab(tab: HomeTab, isStart: Boolean = false, startArgs: Bundle? = null)
    }

    abstract class Presenter: BaseMVPNavPresenter<View>() {
        abstract fun setCurrent(tab: HomeTab, isStart: Boolean = false, force: Boolean = false)
        abstract fun getCurrent(): HomeTab?
    }
}