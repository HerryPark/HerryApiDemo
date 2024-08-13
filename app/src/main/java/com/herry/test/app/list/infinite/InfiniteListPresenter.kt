package com.herry.test.app.list.infinite

import io.reactivex.Observable

class InfiniteListPresenter: InfiniteListContract.Presenter() {
    override fun onResume(view: InfiniteListContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            loadList()
        }
    }

    private fun loadList() {
        subscribeObservable(
            observable = Observable.fromCallable {
                mutableListOf<InfiniteListContract.InfiniteListItem>().apply {
                    for (index in 0 until 10) {
                        add(InfiniteListContract.InfiniteListItem(index = index + 1))
                    }
                }
            },
            onNext = { list ->
                view?.onUpdatedList(list)
            }
        )
    }
}