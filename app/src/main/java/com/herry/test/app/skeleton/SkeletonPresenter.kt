package com.herry.test.app.skeleton

class SkeletonPresenter : SkeletonContract.Presenter() {

    override fun onResume(view: SkeletonContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            show()
        }
    }

    override fun show() {
        view?.onUpdate(SkeletonContract.ContentsModel(true))
    }

    override fun hide() {
        view?.onUpdate(SkeletonContract.ContentsModel(false))
    }
}