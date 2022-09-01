package com.herry.test.app.sample.feeds.search

import com.herry.libs.log.Trace
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.app.sample.feeds.detail.TagFeedsDetailCallData
import com.herry.test.app.sample.repository.database.feed.FeedDB
import com.herry.test.app.sample.repository.database.feed.FeedDBRepository
import com.herry.test.app.sample.repository.database.searchlog.RecentlySearchKeyword
import com.herry.test.app.sample.repository.database.searchlog.RecentlySearchKeywordDB
import com.herry.test.app.sample.repository.database.searchlog.RecentlySearchKeywordDBRepository
import com.herry.test.app.sample.tags.TagsPresenter
import com.herry.test.rx.LastOneObservable
import com.herry.test.rx.RxSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class SearchFeedsPresenter : SearchFeedsContract.Presenter() {

    private var feedDatabase: FeedDB? = null
    private var feedRepository: FeedDBRepository? = null

    private var recentlyDatabase: RecentlySearchKeywordDB? = null
    private var recentlyRepository: RecentlySearchKeywordDBRepository? = null

    private val recentlyKeywordsNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
    private val autocompleteNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
    private val feedsNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    private val autoCompleteCompositeDisposable = CompositeDisposable()
    private val autoCompleteBehaviorSubject = BehaviorSubject.create<String>()

    private val searchResultFeedsDisposable = LastOneObservable<Pair<Boolean, MutableList<SearchFeedsContract.SearchResultData>>>(
        {
            displaySearchFeedResults(it.first, it.second)
        }
    )

    private var currentPosition: Int = 0

    override fun onAttach(view: SearchFeedsContract.View) {
        super.onAttach(view)

        val context = view.getViewContext() ?: return
        feedDatabase = FeedDB.getInstance(context)?.also { db ->
            feedRepository = FeedDBRepository(db.dao())
        }

        recentlyDatabase = RecentlySearchKeywordDB.getInstance(context)?.also { db ->
            recentlyRepository = RecentlySearchKeywordDBRepository(db.dao())
        }

        view.keywordsRoot.beginTransition()
        view.keywordsRoot.clearChild()
        NodeHelper.addNode(view.keywordsRoot, recentlyKeywordsNodes)
        NodeHelper.addNode(view.keywordsRoot, autocompleteNodes)
        view.keywordsRoot.endTransition()

        view.feedsRoot.beginTransition()
        view.feedsRoot.clearChild()
        NodeHelper.addNode(view.feedsRoot, feedsNodes)
        view.feedsRoot.endTransition()

        setAutoCompleteComposite()
    }

    override fun onDetach() {
        autoCompleteCompositeDisposable.dispose()
        searchResultFeedsDisposable.dispose()

        recentlyRepository = null
        recentlyDatabase = null

        feedRepository = null
        feedDatabase = null

        super.onDetach()
    }

    override fun onLaunch(view: SearchFeedsContract.View, recreated: Boolean) {
        launch {
            loadRecentlySearchKeywords()

            getAutoComplete("")
        }
    }

    override fun getAutoComplete(keyword: String) {
        autoCompleteBehaviorSubject.onNext(keyword)
    }

    private fun getAutoCompleteKeywords(keyword: String): Observable<MutableList<String>> {
        return Observable.create { emitter ->

            emitter.onNext(feedRepository?.getAutoCompleteKeywords(keyword) ?: mutableListOf())
            emitter.onComplete()
        }
    }

    private fun setAutoCompleteComposite() {
        view ?: return

        autoCompleteCompositeDisposable.add(
            presenterObservable(
                autoCompleteBehaviorSubject.subscribeOn(RxSchedulerProvider.io())
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .observeOn(RxSchedulerProvider.ui())
                    .concatMap { searchText ->
                        Observable.create<String> {
                            it.onNext(searchText)
                            it.onComplete()
                        }
                    },
                loadView = false
            )
                .subscribe { searchText ->
                    if (searchText.isNotEmpty()) {
//                        Trace.d("Herry", "autoComplete search keyword load : $searchText")
//                        view.onLoadViewVisible(true)
                        subscribeObservable(
                            getAutoCompleteKeywords(searchText),
                            {
                                displaySearchAutoCompletes(it)
//                                view.onLoadViewVisible(false)
                            },
                            loadView = false
                        )
                    } else {
                        view?.onChangedViewMode(SearchFeedsContract.ViewMode.RECOMMEND)
//                        view.onLoadViewVisible(false)
                    }
                }
        )
    }

    private fun displaySearchAutoCompletes(list: MutableList<String>) {
        this.autocompleteNodes.beginTransition()

        val nodes = NodeHelper.createNodeGroup()
        if (list.isNotEmpty()) {
            NodeHelper.addModels(nodes, *list.toTypedArray())
        } else {
            NodeHelper.addModel(nodes, SearchFeedsContract.EmptyModel())
        }
        NodeHelper.upsert(this.autocompleteNodes, nodes)

        this.autocompleteNodes.endTransition()

        view?.onChangedViewMode(SearchFeedsContract.ViewMode.RECOMMEND)
    }

    private fun getRecentlySearchKeywords(): Observable<MutableList<RecentlySearchKeyword>> {
        return Observable.create { emitter ->
            emitter.onNext(recentlyRepository?.getList()?.toMutableList() ?: mutableListOf())
            emitter.onComplete()
        }
    }

    private fun loadRecentlySearchKeywords() {
        subscribeObservable(
            getRecentlySearchKeywords(),
            onNext = {
                displayRecentlySearchKeywords(it)
            },
            loadView = false
        )
    }

    private fun displayRecentlySearchKeywords(list: MutableList<RecentlySearchKeyword>) {
        this.recentlyKeywordsNodes.beginTransition()

        val recentlySearchWordNodes = NodeHelper.createNodeGroup()

        if(list.isNotEmpty()) {
            NodeHelper.addModels(recentlySearchWordNodes, *list.toTypedArray())
        }
        NodeHelper.upsert(this.recentlyKeywordsNodes, recentlySearchWordNodes)

        this.recentlyKeywordsNodes.endTransition()
    }

    private fun getFeeds(): MutableList<SearchFeedsContract.SearchResultData> = NodeHelper.getChildrenModels(feedsNodes)

    override fun loadMoreSearchResults() {
        val keyword = getFeeds().firstOrNull()?.keyword ?: return
        loadFeeds(false, keyword)
    }

    override fun setCurrentPosition(position: Int) {
        this.currentPosition = position
    }

    override fun searchFeeds(keyword: String) {
        view ?: return

        Trace.d("Herry", "searchFeeds $keyword")
        view?.onChangedViewMode(SearchFeedsContract.ViewMode.SEARCH_RESULT)
        loadFeeds(true, keyword)
        // save
    }

    private fun loadFeeds(reset: Boolean, keyword: String) {
        var lastProjectId = ""
        if (!reset) {
            val feeds = getFeeds()
            lastProjectId = if (feeds.isNotEmpty()) feeds.last().feed.projectId else ""
        } else {
            searchResultFeedsDisposable.dispose()
        }

        if (searchResultFeedsDisposable.isDisposed()) {
            if (reset) {
//                view?.onLoadView(true)
            }
            if (keyword.isNotBlank()) {
                searchResultFeedsDisposable.subscribe(
                    presenterObservable(
                        observable = Observable.create<MutableList<SearchFeedsContract.SearchResultData>> { emitter ->
                            val list: MutableList<SearchFeedsContract.SearchResultData> = mutableListOf()

                            feedRepository?.getTagFeeds(tag = keyword, lastProjectId = lastProjectId, pageSize = TagsPresenter.PAGE_SIZE)?.forEach { feed ->
                                list.add(SearchFeedsContract.SearchResultData(keyword, feed))
                            }

                            emitter.onNext(list)
                            emitter.onComplete()
                        }, //.delay((if (init) 500 else 0).toLong(), TimeUnit.MILLISECONDS),
                        loadView = false
                    )
                        .map {
                            if (reset) {
//                            view?.onLoadView(false)
                            }
                            Pair(reset, it)
                        }
                )
            } else {
                displaySearchFeedResults(reset, null)
            }
        }
    }

    private fun displaySearchFeedResults(reset: Boolean, result: MutableList<SearchFeedsContract.SearchResultData>? = null) {
        view ?: return

        this.feedsNodes.beginTransition()
        if (reset || result == null) {
            val nodes = NodeHelper.createNodeGroup()
            if (result != null) {
                Trace.d("Herry", "displaySearchFeedResults ${result.size}")
                NodeHelper.addModels(nodes, *result.toTypedArray())
            }
            NodeHelper.upsert(this.feedsNodes, nodes)
        } else {
            NodeHelper.addModels(this.feedsNodes, *result.toTypedArray())
        }
        this.feedsNodes.endTransition()

        if (reset) {
//            view?.onScrollTo(currentPosition)
        }
    }

    override fun getFeedDetailCallData(selected: SearchFeedsContract.SearchResultData): FeedDetailCallData? {
        view?.getViewContext() ?: return null

        return TagFeedsDetailCallData(
            loadedProjectCounts = getFeeds().size,
            selectedFeed = selected.feed,
            tag = "#${selected.keyword}"
        )
    }
}