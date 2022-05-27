package com.herry.test.app.sample.feeds.list

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.test.app.base.nav.BaseNavPresenter
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.repository.feed.db.Feed

interface FeedsContract {
    interface View : MVPView<Presenter> {
        val categoryFeedsRoot: NodeRoot
        fun onScrollToCategory(position: Int)
    }

    abstract class Presenter: BaseNavPresenter<View>() {
        abstract fun setCurrentCategory(position: Int)
        abstract fun getCategoryName(position: Int): String
        abstract fun getFeedDetailCallData(selectedFeed: Feed): FeedDetailCallData?
    }
}