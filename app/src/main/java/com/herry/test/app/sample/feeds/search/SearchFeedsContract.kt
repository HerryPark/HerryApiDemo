package com.herry.test.app.sample.feeds.search

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.test.app.base.nav.BaseNavPresenter
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.app.sample.repository.database.feed.Feed

interface SearchFeedsContract {
    interface View : MVPView<Presenter> {
        val keywordsRoot: NodeRoot
        val feedsRoot: NodeRoot

        fun onChangedViewMode(mode: ViewMode)
        fun onUpdatedSearchFeedResult()
    }

    abstract class Presenter: BaseNavPresenter<View>() {
        abstract fun getAutoComplete(keyword: String)
        abstract fun searchFeeds(keyword: String)
        abstract fun loadMoreSearchResults()
        abstract fun setCurrentPosition(position: Int)
        abstract fun getFeedDetailCallData(selected: SearchResultData): FeedDetailCallData?
    }

    enum class ViewMode {
        RECOMMEND,
        SEARCH_RESULT
    }

    class EmptyModel

    data class SearchResultData(
        val keyword: String,
        val feed: Feed
    )
}