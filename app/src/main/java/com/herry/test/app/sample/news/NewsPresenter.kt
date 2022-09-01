package com.herry.test.app.sample.news

import com.google.android.exoplayer2.ExoPlayer
import com.herry.libs.media.exoplayer.ExoPlayerManager
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.app.sample.forms.FeedForm
import com.herry.test.app.sample.repository.database.feed.FeedDB
import com.herry.test.app.sample.repository.database.feed.FeedDBRepository
import com.herry.test.rx.LastOneObservable
import com.herry.test.rx.RxUtil
import io.reactivex.Observable


class NewsPresenter : NewsContract.Presenter() {

    companion object {
        const val PAGE_SIZE = 10
    }

    private val lastOneObservable = LastOneObservable<Pair<Boolean, MutableList<FeedForm.Model>>>(
        {
            display(it.first, it.second)
        }
    )

    private var feedDatabase: FeedDB? = null
    private var feedRepository: FeedDBRepository? = null

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
    private var currentPosition: Int = 0

    private val exoPlayerManger: ExoPlayerManager = ExoPlayerManager(
        context = {
            view?.getViewContext()
        },
        isSingleInstance = false
    )

    override fun onAttach(view: NewsContract.View) {
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

    override fun onLaunch(view: NewsContract.View, recreated: Boolean) {
        launch {
            load()
        }
    }

    override fun onResume(view: NewsContract.View) {
        launch {
            load()
        }
    }

    override fun onPause(view: NewsContract.View) {
        launch {
            stopPlayAll()
        }
    }

    private fun load() {
        val videos = getFeeds()
        if (videos.size <= 0) {
            this.load(true)
        } else {
            nodes.beginTransition()
            nodes.clearChild()
            nodes.endTransition()
            display(false, videos)
            view?.onScrollTo(currentPosition)
        }
    }

    private fun load(reset: Boolean) {
        var lastProjectId = ""
        if (!reset) {
            val feeds = getFeeds()
            lastProjectId = if (feeds.isNotEmpty()) feeds.last().feed.projectId else ""
        } else {
            lastOneObservable.dispose()
        }

        if (lastOneObservable.isDisposed()) {
            if (reset) {
                // show load view
            }
            lastOneObservable.subscribe(
                RxUtil.setPresenterObservable(
                    observable = Observable.create<MutableList<FeedForm.Model>> { emitter ->
                        val list: MutableList<FeedForm.Model> = mutableListOf()
                        feedRepository?.getNewFeeds(lastProjectId, PAGE_SIZE)?.forEachIndexed { index, feed ->
                            list.add(FeedForm.Model(index, feed))
                        }

                        emitter.onNext(list)
                        emitter.onComplete()
                    }, //.delay((if (init) 500 else 0).toLong(), TimeUnit.MILLISECONDS),
                    view = this::view,
                    loadView = false
                )
                    .map {
                        if (reset) {
                            // hide load view
                        }
                        Pair(reset, it)
                    }
            )
        }
    }

    private fun display(reset: Boolean, list: MutableList<FeedForm.Model>) {
        this.nodes.beginTransition()
        if(reset) {
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

    override fun preparePlayer(model: FeedForm.Model?): ExoPlayer? {
        model ?: return null

        return exoPlayerManger.prepare(model.feed.projectId, model.feed.videoPath)
    }

    private fun getFeedModelFromFeeds(position: Int): FeedForm.Model?{
        val nodePosition = nodes.getNodePosition(position) ?: return null
        val node = nodes.getNode(nodePosition) ?: return null
        return node.model as? FeedForm.Model
    }

    override fun play(position: Int) {
        val model = getFeedModelFromFeeds(position) ?: return

        exoPlayerManger.play(model.feed.projectId, model.feed.videoPath, true)
    }

    override fun stop(position: Int) {
        val model = getFeedModelFromFeeds(position) ?: return

        exoPlayerManger.stop(model.feed.projectId)
    }

    override fun stop(model: FeedForm.Model?) {
        model ?: return
        exoPlayerManger.stop(model.feed.projectId)
    }

    override fun togglePlay(model: FeedForm.Model?) {
        model ?: return

        val id = model.feed.projectId
        if (exoPlayerManger.isPlaying(id)) {
            // to pause
            exoPlayerManger.pause(id)
        } else if (exoPlayerManger.isReadyToPlay(id)){
            // to resume
            exoPlayerManger.resume(id)
        }
    }

    override fun toggleVolume(model: FeedForm.Model?) {
        if (!exoPlayerManger.isMute()) {
            exoPlayerManger.mute()
        } else {
            exoPlayerManger.unMute()
        }
    }

    private fun stopPlayAll() {
        exoPlayerManger.stopAll()
    }

    override fun loadMore() {
        this.load(false)
    }

    private fun getFeeds(): MutableList<FeedForm.Model> = NodeHelper.getChildrenModels(nodes)
}