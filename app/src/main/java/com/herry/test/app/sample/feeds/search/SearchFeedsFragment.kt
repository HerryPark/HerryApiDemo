package com.herry.test.app.sample.feeds.search

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.libs.widget.view.recyclerview.endless.EndlessRecyclerViewScrollListener
import com.herry.libs.widget.view.recyclerview.form.recycler.RecyclerForm
import com.herry.test.R
import com.herry.test.app.base.ScreenWindowStyle
import com.herry.test.app.base.StatusBarStyle
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.app.sample.feeds.detail.FeedDetailFragment
import com.herry.test.app.sample.repository.database.searchlog.RecentlySearchKeyword
import java.util.*

class SearchFeedsFragment: BaseNavView<SearchFeedsContract.View, SearchFeedsContract.Presenter>(), SearchFeedsContract.View {

    override fun onScreenWindowStyle(context: Context): ScreenWindowStyle = ScreenWindowStyle(false, StatusBarStyle.LIGHT)

    override fun onCreatePresenter(): SearchFeedsContract.Presenter = SearchFeedsPresenter()

    override fun onCreatePresenterView(): SearchFeedsContract.View = this

    private var container: View? = null

    private val keywordForm = SearchInputEditForm(
        onTextChanged = { text ->
            presenter?.getAutoComplete(text)
        },
        onEditorActionListener = { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                AppUtil.pressBackKey(activity, container?.rootView)
                true
            } else {
                false
            }
        })

    override val keywordsRoot: NodeRoot
        get() = keywordsAdapter.root

    private val keywordsAdapter: KeywordsAdapter = KeywordsAdapter()

    private val keywordsRecyclerForm = object : RecyclerForm() {
        override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView) {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
                if (itemAnimator is SimpleItemAnimator) {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when(newState) {
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                // hide softkeyboard and remove focus
                                ViewUtil.hideSoftKeyboard(requireContext(), container?.rootView)
                                keywordForm.requestFocus(false)
                            }
                        }
                    }
                })
            }
            recyclerView.adapter = keywordsAdapter
        }
    }

    override val feedsRoot: NodeRoot
        get() = feedsAdapter.root

    override fun onChangedViewMode(mode: SearchFeedsContract.ViewMode) {
        when (mode) {
            SearchFeedsContract.ViewMode.RECOMMEND -> {
                keywordsRecyclerForm.isVisible(true)
                feedsRecyclerForm.isVisible(false)
            }
            SearchFeedsContract.ViewMode.SEARCH_RESULT -> {
                feedsRecyclerForm.isVisible(true)
                keywordsRecyclerForm.isVisible(false)
            }
        }
    }

    private val feedsAdapter: FeedsAdapter = FeedsAdapter()

    private val feedsRecyclerForm = object : RecyclerForm() {
        override fun onBindRecyclerView(context: Context, recyclerView: RecyclerView) {
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = StaggeredGridLayoutManager(calculateSpanCounts(context), StaggeredGridLayoutManager.VERTICAL)
                if (itemAnimator is SimpleItemAnimator) {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
                val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        presenter?.loadMoreSearchResults()
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

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when(newState) {
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                // hide softkeyboard and remove focus
                                ViewUtil.hideSoftKeyboard(requireContext(), container?.rootView)
                                keywordForm.requestFocus(false)
                            }
                        }
                    }
                }
                addOnScrollListener(scrollListener)
            }
            recyclerView.adapter = feedsAdapter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.search_feeds_fragment, container, false)
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

        view.findViewById<View>(R.id.search_feeds_fragment_search_keyword_form)?.run {
            keywordForm.bindHolder(context, this)
        }

        view.findViewById<View>(R.id.search_feeds_fragment_recently)?.run {
            ViewUtil.setProtectTouchLowLayer(this, true)
            keywordsRecyclerForm.bindHolder(context, this)
        }

        view.findViewById<View>(R.id.search_feeds_fragment_searched_feeds)?.run {
            ViewUtil.setProtectTouchLowLayer(this, true)
            feedsRecyclerForm.bindHolder(context, this)
        }
    }

    override fun onUpdatedSearchFeedResult() {
        Trace.d("onUpdatedSearchFeedResult")
        keywordForm.requestFocus(focus = false, withKeyboard = true)
        // hide soft keyboard
        ViewUtil.hideSoftKeyboard(requireContext(), container)
    }

    inner class KeywordsAdapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
//            list.add(RecentHeadForm())
            list.add(RecentForm(
                onClickKeyword = { model ->
                    presenter?.searchFeeds(model.keyword)
                },
                onClickDelete = { model ->
                }
            ))
            list.add(AutoCompleteForm(
                onClickKeyword = { keyword ->
                    presenter?.searchFeeds(keyword)
                }
            ))
        }
    }

    inner class FeedsAdapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
//            list.add(EmptyForm())
            list.add(SearchResultFeedItemForm(onClickItem = { form, holder ->
                val model = NodeRecyclerForm.getBindModel(form, holder) ?: return@SearchResultFeedItemForm
                presenter?.getFeedDetailCallData(model)?.let { callData ->
                    navigateTo(
                        destinationId = R.id.feed_detail_fragment,
                        args = FeedDetailFragment.createArguments(callData),
                        navigatorExtras = FeedDetailFragment.createNavigatorExtra(holder.cover, model.feed))
                }
            }))
        }
    }

    private inner class RecentForm(
        val onClickKeyword: (keyword: RecentlySearchKeyword) -> Unit,
        val onClickDelete: (keyword: RecentlySearchKeyword) -> Unit,
    ) : NodeForm<RecentForm.Holder, RecentlySearchKeyword>(Holder::class, RecentlySearchKeyword::class) {

        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
//            val icon: ImageView? = view.findViewById(R.id.recent_keyword_list_item_icon)
            val keyword: TextView? = view.findViewById(R.id.recent_keyword_list_item_keyword)
            val delete: View? = view.findViewById(R.id.recent_keyword_list_item_delete)

            init {
                view.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@RecentForm, this@Holder)?.let { model ->
                        onClickKeyword(model)
                    }
                }
                delete?.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@RecentForm, this@Holder)?.let { model ->
                        onClickDelete(model)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.recent_keyword_list_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: RecentlySearchKeyword) {
            holder.keyword?.text = model.keyword
        }
    }

    private inner class AutoCompleteForm(
        val onClickKeyword: (keyword: String) -> Unit,
    ) : NodeForm<AutoCompleteForm.Holder, String>(Holder::class, String::class) {

        inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
            val icon: ImageView? = view.findViewById(R.id.recent_keyword_list_item_icon)
            val keyword: TextView? = view.findViewById(R.id.recent_keyword_list_item_keyword)
            val delete: View? = view.findViewById(R.id.recent_keyword_list_item_delete)

            init {
                icon?.setImageResource(R.drawable.ic_search)
                delete?.isVisible = false
                view.setOnProtectClickListener {
                    NodeRecyclerForm.getBindModel(this@AutoCompleteForm, this@Holder)?.let { model ->
                        onClickKeyword(model)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.recent_keyword_list_item

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: String) {
            holder.keyword?.text = model
        }
    }

    class SearchResultFeedItemForm(
        private val onClickItem: ((form: SearchResultFeedItemForm, holder: Holder) -> Unit)?
    ): NodeForm<SearchResultFeedItemForm.Holder, SearchFeedsContract.SearchResultData>(Holder::class, SearchFeedsContract.SearchResultData::class) {
        inner class Holder(context: Context, view: View): NodeHolder(context, view) {
            val container: View? = view.findViewById(R.id.feed_list_item_form_container)
            val cover: ImageView? = view.findViewById(R.id.feed_list_item_form_cover)

            init {
                container?.setOnProtectClickListener {
                    onClickItem?.invoke(this@SearchResultFeedItemForm, this)
                }
            }
        }

        override fun onLayout(): Int = R.layout.feed_list_item_form

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: SearchFeedsContract.SearchResultData) {
            val feed = model.feed
            val constraintLayout = holder.view as? ConstraintLayout
            if (constraintLayout != null) {
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)
                holder.container?.let { container ->
                    val width = feed.width
                    val height = feed.height
                    val dimensionRatio = String.format(Locale.ENGLISH, "H,%d:%d", width, height)
                    constraintSet.setDimensionRatio(container.id, dimensionRatio)
                    constraintSet.applyTo(constraintLayout)
                }
            }

            holder.cover?.let { cover ->
                Glide.with(context).load(feed.imagePath)
                    .placeholder(ColorDrawable(ViewUtil.getColor(context, R.color.tbc_70)))
                    .into(cover)
            }
        }
    }
}