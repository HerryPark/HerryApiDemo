package com.herry.test.app.sample.feeds.list

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setViewPadding
import com.herry.libs.widget.view.recyclerview.tabrecycler.TabRecyclerContract
import com.herry.libs.widget.view.recyclerview.tabrecycler.TabRecyclerView
import com.herry.libs.widget.view.swiperefreshlayout.SwipeRefreshLayoutEx
import com.herry.test.R
import com.herry.test.app.sample.forms.FeedsItemForm
import com.herry.test.app.sample.repository.database.feed.Feed

class FeedCategoryFeedsForm(
    private val onErrorCB: (throwable: Throwable) -> Unit,
    private val onRefresh: () -> Unit,
    private val onClickFeed: (coverView: ImageView?, feed: Feed) -> Unit
): TabRecyclerView(), TabRecyclerView.OnTabRecyclerViewListener {

    override val listener = this

//    private val loadForm = ShimmerFrameLoadForm(viewLayout = R.layout.cash_history_list_load_container)

    private var swipeRefreshLayout: SwipeRefreshLayoutEx? = null

    override fun getCustomLayout(): Int = R.layout.feed_category_feeds_form

    override fun getCustomRecyclerForm(container: View): View? = container.findViewById(R.id.feed_category_feeds_form_recycler_form)

    private fun calculateSpanCounts(context: Context): Int {
        val defaultMargin = 16 // 16dp
        val defaultSpanCounts = 2
        val screenSize = ViewUtil.getScreenSize(context)
        val spanCounts = if (screenSize.width <= 0) {
            defaultSpanCounts
        } else {
            ((defaultSpanCounts * ViewUtil.convertPxToDp(screenSize.width.toFloat()).toInt()) / (360 - (defaultSpanCounts + 1) * defaultMargin))
        }

        return spanCounts
    }

    override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView, container: View) {
        recyclerView.run {
            setViewPadding(ViewUtil.getDimensionPixelSize(context, R.dimen.size08))
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(calculateSpanCounts(context), StaggeredGridLayoutManager.VERTICAL)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            clipToPadding = false
        }

        swipeRefreshLayout = container.findViewById<SwipeRefreshLayoutEx>(R.id.feed_category_feeds_form_swipe_refresh_layout)?.apply {
            setOnRefreshListener {
                onRefresh()
                isRefreshing = false
            }
            setOnChildScrollUpListener(object : SwipeRefreshLayoutEx.OnChildScrollUpListener {
                override fun canChildScrollUp(): Boolean = recyclerView.canScrollVertically(-1)
            })
            setBlockRefreshingTouch(true)
        }
    }

    override fun onBindHolder(list: MutableList<NodeForm<out NodeHolder, *>>) {
        list.add(FeedsItemForm(onClickItem = { form, holder ->
            val feed = NodeRecyclerForm.getBindModel(form, holder) ?: return@FeedsItemForm
            onClickFeed.invoke(holder.cover, feed)
        }))
    }

    override fun onScrollStateChanged(holder: Holder, recyclerView: RecyclerView, newState: Int) {
    }

    override fun onScrolled(holder: Holder, recyclerView: RecyclerView, dx: Int, dy: Int) {
        val presenter = holder.presenter ?: return
//        Trace.d("onScrolled presenter (${(holder.presenter as? FeedCategoryFeedsPresenter)?.category})")
        val spanCount = (recyclerView.layoutManager as? StaggeredGridLayoutManager)?.spanCount ?: return
        val positionArray = IntArray(spanCount)
        val firstVisiblePosition =(recyclerView.layoutManager as? StaggeredGridLayoutManager)?.findFirstCompletelyVisibleItemPositions(positionArray) ?: positionArray

        if (firstVisiblePosition.isNotEmpty()) {
            val firstItemPosition = positionArray[0]
            if (firstItemPosition >= 0) {
                presenter.setCurrentPosition(position = firstItemPosition)
            }
        }
    }

    override fun onScrollTop(holder: Holder) {
    }

    override fun onBindEmptyView(context: Context, parent: ViewGroup?, visible: Boolean): View? {
        return null
//        return if (!visible) {
//            null
//        } else {
//            val emptyForm = CashHistoryEmptyForm()
//            emptyForm.createFormHolder(context, parent)
//            emptyForm.bindFormModel(context, CashHistoryEmptyForm.Model())
//            emptyForm.getView()
//        }
    }

    override fun onBindLoadView(context: Context, parent: ViewGroup?, visible: Boolean): View? {
        return null
//        return if (!visible) {
//            loadForm.bindFormModel(context, ShimmerFrameLoadForm.Model(false))
//            null
//        } else {
//            loadForm.createFormHolder(context, parent)
//            loadForm.bindFormModel(context, ShimmerFrameLoadForm.Model(true))
//            loadForm.getView()
//        }
    }

    override fun onError(throwable: Throwable) {
        onErrorCB(throwable)
    }

    override fun onAttachedTabRecyclerView(view: TabRecyclerContract.View, presenter: TabRecyclerContract.Presenter) {
//        if (presenter !is FeedCategoryFeedsPresenter) return
//        Trace.d("onAttachedTabRecyclerView ${presenter.category}")
    }

    override fun onDetachedTabRecyclerView(view: TabRecyclerContract.View, presenter: TabRecyclerContract.Presenter) {
//        if (presenter !is FeedCategoryFeedsPresenter) return
//        Trace.d("onDetachedTabRecyclerView ${presenter.category}")
    }

    override fun onRecycledTabRecyclerView(view: TabRecyclerContract.View, presenter: TabRecyclerContract.Presenter) {
//        if (presenter !is FeedCategoryFeedsPresenter) return
//        Trace.d("onRecycledTabRecyclerView ${presenter.category}")
    }
}