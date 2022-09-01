package com.herry.test.app.sample.feeds.list

import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.widget.view.recyclerview.tabrecycler.TabRecyclerContract
import com.herry.libs.widget.view.recyclerview.tabrecycler.TabRecyclerLoadingType
import com.herry.libs.widget.view.recyclerview.tabrecycler.TabRecyclerPresenter
import com.herry.test.app.sample.data.FeedCategory
import com.herry.test.app.sample.repository.database.feed.Feed
import com.herry.test.app.sample.repository.database.feed.FeedDB
import com.herry.test.app.sample.repository.database.feed.FeedDBRepository
import com.herry.test.rx.LastOneObservable
import com.herry.test.rx.RxUtil
import io.reactivex.Observable

class FeedCategoryFeedsPresenter(val category: FeedCategory) : TabRecyclerPresenter() {

    private val lastOneObservable = LastOneObservable<Pair<Boolean, MutableList<Feed>>>(
        {
            display(it.first, it.second)
        }
    )

    private var feedDatabase: FeedDB? = null
    private var feedRepository: FeedDBRepository? = null

    private var currentPosition: Int = 0

    override fun onAttach(view: TabRecyclerContract.View) {
        super.onAttach(view)

        val context = view.getViewContext() ?: return
        feedDatabase = FeedDB.getInstance(context)?.also { db ->
            feedRepository = FeedDBRepository(db.dao())
        }
    }

    override fun onDetach() {
        lastOneObservable.dispose()

        feedRepository = null
        feedDatabase = null

        super.onDetach()
    }

    override fun onLaunch() {
//        Trace.d("Herry", "onLaunch : $category")
        loadFeeds(true)
    }

    override fun onResume() {
//        Trace.d("Herry", "onResume  $category to : $currentPosition")
        if (isEmpty()) {
            loadFeeds(true)
        } else {
            view?.onScrollToPosition(currentPosition)
        }
    }

    override fun loadMore() {
        loadFeeds(false)
    }

    override fun refresh(loading: TabRecyclerLoadingType) {
    }

    override fun setCurrentPosition(position: Int) {
        this.currentPosition = position
//        Trace.d("Herry", "scrollToPosition  $category to : $currentPosition")
    }

    override fun onPause() {
    }

    override fun relaunched(recreated: Boolean) {
    }

    private fun loadFeeds(init: Boolean) {
        var lastProjectId = ""
        if (!init) {
            val feeds = getFeeds()
            lastProjectId = if (feeds.isNotEmpty()) feeds.last().projectId else ""
        } else {
            lastOneObservable.dispose()
        }

        if (lastOneObservable.isDisposed()) {
            if (init) {
                view?.onLoadView(true)
            }
            lastOneObservable.subscribe(
                RxUtil.setPresenterObservable(
                    observable = Observable.create<MutableList<Feed>> { emitter ->
                        val list: MutableList<Feed> = mutableListOf()

                        feedRepository?.getList(category = category.id, lastProjectId = lastProjectId)?.let { feeds ->
                            list.addAll(feeds)
                        }

                        emitter.onNext(list)
                        emitter.onComplete()
                    }, //.delay((if (init) 500 else 0).toLong(), TimeUnit.MILLISECONDS),
                    view = this::view,
                    loadView = false
                )
                    .map {
                        if (init) {
                            view?.onLoadView(false)
                        }
                        Pair(init, it)
                    }
            )
        }
    }

    private fun display(init: Boolean, list: MutableList<Feed>) {
        var showEmpty = false
        // sets results
        this.nodes.beginTransition()
        if (init) {
            val nodes = NodeHelper.createNodeGroup()
            NodeHelper.addModels(nodes, *list.toTypedArray())
            NodeHelper.upsert(this.nodes, nodes)

            showEmpty = list.isEmpty()
        } else if(list.isNotEmpty()){
            NodeHelper.addModels(nodes, *list.toTypedArray())
        }
        this.nodes.endTransition()

        if (init) {
            view?.onEmptyView(showEmpty)
            view?.onScrollToPosition(currentPosition)
        }
    }

    fun getFeeds(): MutableList<Feed> = NodeHelper.getChildrenModels(nodes)
}