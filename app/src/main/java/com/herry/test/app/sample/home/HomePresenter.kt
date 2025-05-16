package com.herry.test.app.sample.home

import com.herry.libs.log.Trace

class HomePresenter : HomeContract.Presenter() {
    private var currentScreen: HomeTab? = null

    override fun onResume(view: HomeContract.View, state: ResumeState) {
    }

    override fun setCurrent(tab: HomeTab, isStart: Boolean, force: Boolean) {
        if (this.currentScreen == tab && !force) {
            return
        }

        this.currentScreen = tab

        view?.onSelectTab(
            tab = tab,
            isStart = isStart
        )
    }

    override fun getCurrent(): HomeTab? = this.currentScreen
}