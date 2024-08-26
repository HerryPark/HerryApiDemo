package com.herry.test.app.list.infinite

import com.herry.libs.mvp.MVPView
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface InfiniteListContract {
    interface View: MVPView<Presenter> {
        fun onUpdatedList(items: List<InfiniteListItem>)
    }

    abstract class Presenter: BaseMVPPresenter<View>()

    data class InfiniteListItem(
        val index: Int
    )
}