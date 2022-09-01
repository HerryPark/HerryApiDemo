package com.herry.test.app.sample.feeds.list

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.app.sample.data.FeedCategory
import com.herry.test.app.sample.feeds.detail.CategoryFeedsDetailCallData
import com.herry.test.app.sample.feeds.detail.FeedDetailCallData
import com.herry.test.app.sample.repository.database.feed.Feed
import com.herry.test.app.sample.repository.database.feed.FeedDB
import com.herry.test.app.sample.repository.database.feed.FeedDBRepository
import io.reactivex.Observable

class FeedsPresenter : FeedsContract.Presenter() {

    private var feedDatabase: FeedDB? = null
    private var feedRepository: FeedDBRepository? = null

    private val categoryFeedsNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    private var currentCategoryPosition: Int = 0

    override fun onAttach(view: FeedsContract.View) {
        super.onAttach(view)

        val context = view.getViewContext() ?: return
        feedDatabase = FeedDB.getInstance(context)?.also { db ->
            feedRepository = FeedDBRepository(db.dao())
        }

        view.categoryFeedsRoot.beginTransition()
        NodeHelper.addNode(view.categoryFeedsRoot, categoryFeedsNodes)
        view.categoryFeedsRoot.endTransition()
    }

    override fun onDetach() {
        feedRepository = null
        feedDatabase = null

        super.onDetach()
    }

    override fun onLaunch(view: FeedsContract.View, recreated: Boolean) {
        launch {
            load(!recreated)
        }
    }

    override fun onResume(view: FeedsContract.View) {
        launch {
            load(false)
        }
    }

    private fun loadFeedCategories(): Observable<MutableList<FeedCategory>> {
        return Observable.create { emitter ->
            emitter.onNext(FeedCategory.values().toMutableList())
            emitter.onComplete()
        }
    }

    private fun loadFeeds(categories: MutableList<FeedCategory>): Observable<MutableList<FeedCategoryFeedsPresenter>> {
        return Observable.create { emitter ->
            val list: MutableList<FeedCategoryFeedsPresenter> = mutableListOf()
            categories.forEach { category ->
                list.add(FeedCategoryFeedsPresenter(category))
            }
            emitter.onNext(list)
            emitter.onComplete()
        }
    }

    private fun load(init: Boolean) {
        if (init) {
            // load categories
            subscribeObservable(
                loadFeedCategories().flatMap { categories ->
                    this.currentCategoryPosition = updateCurrentCategoryPosition(categories.size, currentCategoryPosition)
                    loadFeeds(categories)
                }, { categoryFeedPresenters ->
                    displayCategoryFeeds(categoryFeedPresenters)
                })
        } else {
            view?.onScrollToCategory(currentCategoryPosition)
        }
    }

    private fun updateCurrentCategoryPosition(categoryCounts: Int, currentCategoryPosition: Int): Int {
        var selectedCategoryPosition = currentCategoryPosition
        if (categoryCounts - 1 < selectedCategoryPosition) {
            selectedCategoryPosition = categoryCounts - 1
        }
        if (selectedCategoryPosition < 0) {
            selectedCategoryPosition = 0
        }
        return selectedCategoryPosition
    }

    private fun displayCategoryFeeds(categoryFeedsPresenters: MutableList<FeedCategoryFeedsPresenter>) {
        this.categoryFeedsNodes.beginTransition()
        val nodes = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *categoryFeedsPresenters.toTypedArray())
        NodeHelper.upsert(this.categoryFeedsNodes, nodes)
        this.categoryFeedsNodes.endTransition()

        view?.onScrollToCategory(currentCategoryPosition)
    }

    override fun getCategoryName(position: Int): String {
        return try {
            val feedCategoryFeedsPresenter = this.categoryFeedsNodes.getChildNode(position)?.model as? FeedCategoryFeedsPresenter
            feedCategoryFeedsPresenter?.category?.title ?: "???"
        } catch (ex: IndexOutOfBoundsException) {
            "??"
        }
    }

    override fun setCurrentCategory(position: Int) {
        try {
            val categoryFeedsPresenters = NodeHelper.getChildrenModels<FeedCategoryFeedsPresenter>(this.categoryFeedsNodes)
            val feedCategoryFeedsPresenter = categoryFeedsPresenters[position]
            feedCategoryFeedsPresenter.setCurrentPresent()

            this.currentCategoryPosition = updateCurrentCategoryPosition(categoryFeedsPresenters.size, position)
        } catch (ex: IndexOutOfBoundsException) {
        }
    }

    override fun getFeedDetailCallData(selectedFeed: Feed): FeedDetailCallData? {
        view?.getViewContext() ?: return null

        val currentFeedCategoryFeedsPresenter = try {
            this.categoryFeedsNodes.getChildNode(currentCategoryPosition)?.model as? FeedCategoryFeedsPresenter
        } catch (ex: IndexOutOfBoundsException) {
            null
        }

        currentFeedCategoryFeedsPresenter ?: return null

        val category: FeedCategory = currentFeedCategoryFeedsPresenter.category
        return CategoryFeedsDetailCallData(
            loadedProjectCounts = currentFeedCategoryFeedsPresenter.getFeeds().size,
            selectedFeed = selectedFeed,
            feedsCategory = category
        )
    }
}