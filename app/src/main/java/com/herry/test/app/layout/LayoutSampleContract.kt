package com.herry.test.app.layout

import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter

/**
 * Created by herry.park on 2020/08/19.
 **/
interface LayoutSampleContract {

    interface View : MVPView<Presenter> {
        fun onUpdateRatios(selected: AspectRatioType?)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun selectRatio(type: AspectRatioType?)
    }

    enum class AspectRatioType {
        RATIO_16v9,
        RATIO_9v16,
        RATIO_1v1,
        RATIO_4v3,
        RATIO_3v4,
        RATIO_4v5,
        RATIO_2_35v1
    }

    data class Model(val type: AspectRatioType, var selected: Boolean = false)
}