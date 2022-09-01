package com.herry.test.app.sample.tags

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.app.sample.feeds.detail.TagFeedsDetailCallData
import com.herry.test.app.sample.repository.database.feed.Feed
import com.herry.test.app.sample.repository.database.feed.FeedDB
import com.herry.test.app.sample.repository.database.feed.FeedDBRepository
import com.herry.test.rx.LastOneObservable
import com.herry.test.rx.RxUtil
import io.reactivex.Observable

class TagsPresenter(private val tag: String) : TagsContract.Presenter(){

    companion object {
        const val PAGE_SIZE = 30
    }

    private val lastOneObservable = LastOneObservable<Pair<Boolean, MutableList<Feed>>>(
        {
            display(it.first, it.second)
        }
    )

    private var feedDatabase: FeedDB? = null
    private var feedRepository: FeedDBRepository? = null

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    private var currentPosition: Int = 0

    override fun onAttach(view: TagsContract.View) {
        super.onAttach(view)

        val context = view.getViewContext() ?: return
        feedDatabase = FeedDB.getInstance(context)?.also { db ->
            feedRepository = FeedDBRepository(db.dao())
        }

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onDetach() {
        lastOneObservable.dispose()

        feedRepository = null
        feedDatabase = null

        super.onDetach()
    }

    override fun onLaunch(view: TagsContract.View, recreated: Boolean) {
        launch {
            view.onUpdateTitle(tag)
            load()
        }
    }

    private fun load() {
        val feeds = getFeeds()
        if (feeds.size <= 0) {
            this.load(true)
        } else {
            nodes.beginTransition()
            nodes.clearChild()
            nodes.endTransition()
            display(false, feeds)
            view?.onScrollTo(currentPosition)
        }
    }

    private fun load(reset: Boolean) {
        var lastProjectId = ""
        if (!reset) {
            val feeds = getFeeds()
            lastProjectId = if (feeds.isNotEmpty()) feeds.last().projectId else ""
        } else {
            lastOneObservable.dispose()
        }

        if (lastOneObservable.isDisposed()) {
            if (reset) {
//                view?.onLoadView(true)
            }
            lastOneObservable.subscribe(
                RxUtil.setPresenterObservable(
                    observable = Observable.create<MutableList<Feed>> { emitter ->
                        val list: MutableList<Feed> = mutableListOf()

                        feedRepository?.getTagFeeds(tag = tag, lastProjectId = lastProjectId, pageSize = PAGE_SIZE)?.let { feeds ->
                            list.addAll(feeds)
                        }

                        emitter.onNext(list)
                        emitter.onComplete()
                    }, //.delay((if (init) 500 else 0).toLong(), TimeUnit.MILLISECONDS),
                    view = this::view,
                    loadView = false
                )
                    .map {
                        if (reset) {
//                            view?.onLoadView(false)
                        }
                        Pair(reset, it)
                    }
            )
        }
    }

    private fun display(reset: Boolean, list: MutableList<Feed>) {
        this.nodes.beginTransition()
        if (reset) {
            val nodes = NodeHelper.createNodeGroup()
            NodeHelper.addModels(nodes, *list.toTypedArray())
            NodeHelper.upsert(this.nodes, nodes)
        } else {
            NodeHelper.addModels(this.nodes, *list.toTypedArray())
        }
        this.nodes.endTransition()

        if (reset) {
            view?.onLaunched(this.nodes.getChildCount())
            view?.onScrollTo(currentPosition)
        }
    }

    override fun setCurrentPosition(position: Int) {
        this.currentPosition = position
    }

    override fun loadMore() {
        load(false)
    }

    override fun getFeedDetailCallData(selectedFeed: Feed): FeedDetailCallData? {
        view?.getViewContext() ?: return null

        return TagFeedsDetailCallData(
            loadedProjectCounts = getFeeds().size,
            selectedFeed = selectedFeed,
            tag = tag
        )
    }

    private fun getFeeds(): MutableList<Feed> = NodeHelper.getChildrenModels(nodes)
}