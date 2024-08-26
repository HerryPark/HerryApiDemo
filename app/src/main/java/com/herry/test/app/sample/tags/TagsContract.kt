package com.herry.test.app.sample.tags

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.nav.BaseMVPNavPresenter
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.app.sample.repository.database.feed.Feed

interface TagsContract {
    interface View : MVPView<Presenter>, INodeRoot {
        fun onUpdateTitle(title: String)
        fun onLaunched(count: Int)
        fun onScrollTo(position: Int)
    }

    abstract class Presenter: BaseMVPNavPresenter<View>() {
        abstract fun setCurrentPosition(position: Int)
        abstract fun loadMore()
        abstract fun getFeedDetailCallData(selectedFeed: Feed): FeedDetailCallData?
    }
}