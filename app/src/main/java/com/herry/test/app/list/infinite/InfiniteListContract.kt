package com.herry.test.app.list.infinite

import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BasePresenter

interface InfiniteListContract {
    interface View: MVPView<Presenter> {
        fun onUpdatedList(items: List<InfiniteListItem>)
    }

    abstract class Presenter: BasePresenter<View>()

    data class InfiniteListItem(
        val index: Int
    )
}