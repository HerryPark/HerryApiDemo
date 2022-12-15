package com.herry.test.app.sample.home

import com.herry.test.R
import com.herry.test.app.sample.home.form.HomeBottomNavControlForm
import com.herry.test.app.sample.home.form.HomeBottomNavControlItemForm
import com.herry.test.app.sample.home.form.HomeBottomNavScreenId

class HomePresenter : HomeContract.Presenter() {
    private var currentScreen: HomeBottomNavScreenId? = null
    private val tabs = arrayListOf(
        // mix
        HomeBottomNavControlItemForm.Model(id = R.id.sample_news_navigation, icon = R.drawable.ic_navigation_new, "New"),
        // mix
        HomeBottomNavControlItemForm.Model(id = R.id.sample_feeds_navigation, icon = R.drawable.ic_navigation_feeds, "Feeds"),
        // mix
        HomeBottomNavControlItemForm.Model(id = R.id.sample_create_navigation, icon = R.drawable.ic_navigation_create, "Create"),
        // mix
        HomeBottomNavControlItemForm.Model(id = R.id.sample_me_navigation, icon = R.drawable.ic_navigation_me, "Me")
    )

    override fun onResume(view: HomeContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            loadNavigator(state == ResumeState.LAUNCH)
        }
    }

    private fun loadNavigator(init: Boolean) {
        displayNavigator(false, init)
    }

    private fun displayNavigator(hasNewNotice: Boolean, isStart: Boolean) {
        view?.getViewContext() ?: return

        val currentScreen = this.currentScreen ?: HomeBottomNavScreenId.CREATE

        setCurrent(currentScreen, isStart = isStart, force = true)
    }

    override fun setCurrent(id: HomeBottomNavScreenId, isStart: Boolean, force: Boolean) {
        if (this.currentScreen == id && !force) {
            return
        }

        this.currentScreen = id

        view?.onSelectTab(
            model = HomeBottomNavControlForm.Model(
                selected = id,
                items = tabs
            ),
            isStart = isStart
        )
    }

    override fun getCurrent(): HomeBottomNavScreenId? = this.currentScreen
}