package com.herry.test.app.skeleton

import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface SkeletonContract {
    interface View : MVPView<Presenter> {
        fun onUpdate(model: ContentsModel)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun show()
        abstract fun hide()
    }

    data class ContentsModel(
        val show: Boolean
    )
}