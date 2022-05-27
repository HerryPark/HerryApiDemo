package com.herry.test.app.sample.home

import android.os.Bundle
import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.nav.BaseNavPresenter
import com.herry.test.app.sample.home.form.HomeBottomNavControlForm
import com.herry.test.app.sample.home.form.HomeBottomNavScreenId

interface HomeContract {
    interface View : MVPView<Presenter> {
        fun onSelectTab(model: HomeBottomNavControlForm.Model, isStart: Boolean = false, startArgs: Bundle? = null)
    }

    abstract class Presenter: BaseNavPresenter<View>() {
        abstract fun setCurrent(id: HomeBottomNavScreenId, isStart: Boolean = false, force: Boolean = false)
        abstract fun getCurrent(): HomeBottomNavScreenId?
    }
}