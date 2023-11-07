package com.herry.test.app.sample.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.util.BundleUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.view.recyclerview.endless.EndlessRecyclerViewScrollListener
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerViewForm
import com.herry.libs.widget.view.swiperefreshlayout.SwipeRefreshLayoutEx
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.app.sample.feeds.detail.FeedDetailFragment
import com.herry.test.app.sample.forms.FeedsItemForm
import com.herry.test.widget.TitleBarForm

class TagsFragment : BaseNavView<TagsContract.View, TagsContract.Presenter>(), TagsContract.View {

    companion object {
        private const val ARG_TAG = "ARG_TAG"

        fun createArguments(tag: String): Bundle = Bundle().apply {
            putString(ARG_TAG, tag)
        }

        private fun getTag(args: Bundle?): String? = BundleUtil[args, ARG_TAG, String::class]
    }

    override fun onCreatePresenter(): TagsContract.Presenter? {
        val tag = getTag(getDefaultArguments()) ?: return null
        return TagsPresenter(tag)
    }

    override fun onCreatePresenterView(): TagsContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    private val titleBarForm: TitleBarForm = TitleBarForm(
        activity = { requireActivity() },
        onClickBack = { AppUtil.pressBackKey(requireActivity(), container) }
    )

    private var recyclerForm = object: RecyclerViewForm() {
        override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView) {
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(calculateSpanCounts(context), StaggeredGridLayoutManager.VERTICAL)
                if (itemAnimator is SimpleItemAnimator) {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                clipToPadding = false

                val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        presenter?.loadMore()
                    }

                    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                        val spanCount = (recyclerView.layoutManager as? StaggeredGridLayoutManager)?.spanCount ?: return
                        val positionArray = IntArray(spanCount)
                        val firstVisiblePosition =(recyclerView.layoutManager as? StaggeredGridLayoutManager)?.findFirstCompletelyVisibleItemPositions(positionArray) ?: positionArray

                        if (firstVisiblePosition.isNotEmpty()) {
                            val firstItemPosition = positionArray[0]
                            if (firstItemPosition >= 0) {
                                presenter?.setCurrentPosition(position = firstItemPosition)
                            }
                        }
                        super.onScrolled(view, dx, dy)
                    }
                }
                addOnScrollListener(scrollListener)
                endlessRecyclerViewScrollListener = scrollListener

                container?.findViewById<SwipeRefreshLayoutEx>(R.id.tags_fragment_swipe_refresh_layout)?.apply {
                    setOnRefreshListener {
//                onRefresh()
                        isRefreshing = false
                    }
                    setOnChildScrollUpListener(object : SwipeRefreshLayoutEx.OnChildScrollUpListener {
                        override fun canChildScrollUp(): Boolean = recyclerView.canScrollVertically(-1)
                    })
                    setBlockRefreshingTouch(true)
                }

                adapter = this@TagsFragment.adapter
            }
        }

        private fun calculateSpanCounts(context: Context): Int {
            val defaultMargin = 16 // 16dp
            val defaultSpanCounts = 2
            val screenSize = ViewUtil.getScreenSize(context)
            val spanCounts = if (screenSize.width <= 0) {
                defaultSpanCounts
            } else {
                ((defaultSpanCounts * ViewUtil.convertPixelsToDp(screenSize.width.toFloat()).toInt()) / (360 - (defaultSpanCounts + 1) * defaultMargin))
            }

            return spanCounts
        }
    }

    private var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.tags_fragment, container, false)
            init(this.container)
        } else {
            // fixed: "java.lang.IllegalStateException: The specified child already has a parent.
            // You must call removeView() on the child's parent first."
            ViewUtil.removeViewFormParent(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        view.findViewById<View>(R.id.tags_fragment_title)?.let {
            titleBarForm.bindFormHolder(context, it)
        }

        view.findViewById<View>(R.id.tags_fragment_list)?.let {
            recyclerForm.bindHolder(context, it)
        }
    }

    override fun onUpdateTitle(title: String) {
        val context = this.context ?: return

        titleBarForm.bindFormModel(context, TitleBarForm.Model(title = title, backEnable = true))
    }

    override fun onLaunched(count: Int) {
        if (0 < count) {
            endlessRecyclerViewScrollListener?.resetState()
        }
    }

    override fun onScrollTo(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            recyclerForm.scrollToPosition(position)
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FeedsItemForm(onClickItem = { form, holder ->
                val feed = NodeRecyclerForm.getBindModel(form, holder) ?: return@FeedsItemForm
                presenter?.getFeedDetailCallData(feed)?.let { callData ->
                    navigateTo(
                        destinationId = R.id.feed_detail_fragment,
                        args = FeedDetailFragment.createArguments(callData),
                        navigatorExtras = FeedDetailFragment.createNavigatorExtra(holder.cover, feed))
                }
            }))
        }
    }
}