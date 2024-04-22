package com.herry.test.app.sample.feeds.detail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.AppUtil
import com.herry.libs.util.BundleUtil
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.anim.ViewAnimCreator
import com.herry.libs.widget.anim.ViewAnimListener
import com.herry.libs.widget.anim.ViewAnimPlayer
import com.herry.libs.widget.configure.SystemUIAppearance
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.configure.SystemUIVisibility
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setViewMarginTop
import com.herry.libs.widget.view.recyclerview.endless.EndlessRecyclerViewScrollListener
import com.herry.libs.widget.view.recyclerview.snap.PagerSnapExHelper
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavView
import com.herry.test.app.sample.forms.FeedForm
import com.herry.test.app.sample.repository.database.feed.Feed
import com.herry.test.app.sample.tags.TagsFragment
import com.herry.test.widget.TitleBarForm
import java.util.Locale

class FeedDetailFragment: BaseNavView<FeedDetailContract.View, FeedDetailContract.Presenter>(), FeedDetailContract.View {

    companion object {
        private var sharedElementTransitionName: String? = null

        private const val ARG_CALL_DATA = "ARG_CALL_DATA"
        private const val STATE_COVER_IMAGE_VISIBLE = "STATE_COVER_IMAGE_VISIBLE"

        fun createArguments(callData: FeedDetailCallData?): Bundle = Bundle().apply {
            if (callData != null) {
                putSerializable(ARG_CALL_DATA, callData)
            }
        }

        fun getCallData(args: Bundle?): FeedDetailCallData? = BundleUtil.getSerializableData(args, ARG_CALL_DATA, FeedDetailCallData::class)

        fun createNavigatorExtra(sharedElementCoverImage: ImageView?, feed: Feed): FragmentNavigator.Extras {
            val sharedElements: MutableList<Pair<View, String>> =  mutableListOf<Pair<View, String>>().apply {
                if (sharedElementCoverImage != null) {
                    val transitionName = feed.projectId
                    sharedElementCoverImage.transitionName = transitionName
                    add(Pair(sharedElementCoverImage, transitionName))

                    sharedElementTransitionName = transitionName
                }
            }

            return FragmentNavigatorExtras(*sharedElements.toTypedArray())
        }
    }

    override fun onSystemUIAppearances(context: Context): SystemUIAppearances =
        SystemUIAppearances.getDefaultSystemUIAppearances(context).apply {
            isFullScreen = true
            statusBar?.backgroundColor = Color.TRANSPARENT
            statusBar?.visibility = SystemUIVisibility.SHOW
        }

    @OptIn(UnstableApi::class)
    override fun onCreatePresenter(): FeedDetailContract.Presenter? {
        val callData = getCallData(arguments) ?: return null
        this.callData = callData
        return FeedDetailPresenter(callData)
    }

    override fun onCreatePresenterView(): FeedDetailContract.View = this

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    private var container: View? = null

    private var snapHelper: PagerSnapExHelper? = null

    private var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener? = null

    private var coverImage: ImageView? = null

    private var callData: FeedDetailCallData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.feed_detail_fragment, container, false)
            init(this.container, savedInstanceState)
        } else {
            // fixed: "java.lang.IllegalStateException: The specified child already has a parent.
            // You must call removeView() on the child's parent first."
            ViewUtil.removeViewFormParent(this.container)
        }
        return this.container
    }

    private fun init(view: View?, savedInstanceState: Bundle?) {
        val context = view?.context ?: return

        coverImage = view.findViewById<ImageView>(R.id.feed_detail_fragment_cover)?.apply {
            transitionName = sharedElementTransitionName
            isVisible = isCoverImageVisible(savedInstanceState)
        }

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
                bindFormHolder(view.context, view.findViewById<View?>(R.id.feed_detail_fragment_title)?.apply {
                    this.setViewMarginTop(ViewUtil.getStatusBarHeight(context))
                })
                bindFormModel(view.context, TitleBarForm.Model(backEnable = true, backgroundColor = Color.TRANSPARENT))
        }

        view.findViewById<RecyclerView>(R.id.feed_detail_fragment_list)?.let { recyclerView ->
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.setHasFixedSize(true)
            if (recyclerView.itemAnimator is SimpleItemAnimator) {
                (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            recyclerView.adapter = this@FeedDetailFragment.adapter

            snapHelper = PagerSnapExHelper().apply {
                setOnSnappedListener(object: PagerSnapExHelper.OnSnappedListener {
                    override fun onSnapped(position: Int, itemCount: Int) {
                        presenter?.setCurrentPosition(position)
                        presenter?.play(position)
                    }

                    override fun onUnsnapped(position: Int, itemCount: Int) {
//                        presenter?.stop(position)
                    }
                })
            }

            snapHelper?.attachToRecyclerView(recyclerView)

            endlessRecyclerViewScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    presenter?.loadMore()
                }
            }.also { listener ->
                recyclerView.addOnScrollListener(listener)
            }
        }
    }

    private fun isCoverImageVisible(savedInstanceState: Bundle?): Boolean = BundleUtil[savedInstanceState, STATE_COVER_IMAGE_VISIBLE, true]

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_COVER_IMAGE_VISIBLE, coverImage?.isVisible ?: false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCoverImageVisible(savedInstanceState)) {
            showCover(context, view, callData?.selectedFeed)
        }
    }

    private fun showCover(context: Context?, container: View?, feed: Feed?) {
        context ?: return
        feed ?: return
        val constraintLayout = container as? ConstraintLayout
        if (constraintLayout != null) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            coverImage?.let { cover ->
                val width = feed.width
                val height = feed.height
                val dimensionRatio = String.format(Locale.ENGLISH, "${if (ViewUtil.isPortraitOrientation(context)) "H" else "W"},%d:%d", width, height)
                constraintSet.setDimensionRatio(cover.id, dimensionRatio)
                constraintSet.applyTo(constraintLayout)

                Glide.with(context).load(feed.imagePath).into(cover)
            }
        }
    }

    private fun hideCover() {
        val cover = coverImage ?: return
        if (cover.isVisible && cover.alpha == 1f) {
            ViewAnimPlayer().apply {
                add(
                    ViewAnimCreator(cover)
                    .alpha(1f, 0f)
                    .duration(1000L))
                onStopListener = object : ViewAnimListener.OnStop {
                    override fun onStop() {
                        cover.isVisible = false
                    }
                }
            }.also {
                it.start()
            }
        } else {
            cover.isVisible = false
        }
    }

    override fun onLaunched(count: Int) {
        if (0 < count) {
            endlessRecyclerViewScrollListener?.resetState()
        }

        hideCover()
    }

    override fun onScrollTo(position: Int) {
        snapHelper?.scrollToSnapPosition(position, true)
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(FeedForm(
                onAttachedVideoView = { model ->
                    presenter?.preparePlayer(model)
                },
                onDetachedVideoView = { model ->
                    presenter?.stop(model)
                },
                onTogglePlayer = { form, holder ->
                    presenter?.togglePlay(NodeRecyclerForm.getBindModel(form, holder))
                },
                onTogglePlayerVolume = { form, holder ->
                    presenter?.let { presenter ->
                        presenter.toggleVolume(NodeRecyclerForm.getBindModel(form, holder))
                        true
                    } ?: false
                },
                onClickTag = { text ->
                    navigateTo(destinationId = R.id.tags_fragment, args = TagsFragment.createArguments(text))
                }
            ))
        }
    }

}