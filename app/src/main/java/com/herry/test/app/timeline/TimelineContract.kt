package com.herry.test.app.timeline

import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface TimelineContract {

    interface View : MVPView<Presenter> {

    }

    abstract class Presenter : BaseMVPPresenter<View>() {

    }

}