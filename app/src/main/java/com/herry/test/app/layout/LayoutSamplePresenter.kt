package com.herry.test.app.layout

/**
 * Created by herry.park on 2020/08/19.
 **/
class LayoutSamplePresenter : LayoutSampleContract.Presenter() {

    private var selectedRatio: LayoutSampleContract.AspectRatioType? = null

    override fun onResume(view: LayoutSampleContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            displayRatios(selectedRatio)
        }
    }

    override fun selectRatio(type: LayoutSampleContract.AspectRatioType?) {
        selectedRatio = type
        displayRatios(selectedRatio)
    }

    private fun displayRatios(type: LayoutSampleContract.AspectRatioType?) {
        view?.getViewContext() ?: return

        view?.onUpdateRatios(type)
    }
}