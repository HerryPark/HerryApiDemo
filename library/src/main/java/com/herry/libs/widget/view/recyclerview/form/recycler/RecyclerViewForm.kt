package com.herry.libs.widget.view.recyclerview.form.recycler

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.herry.libs.R
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.NodeView
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.view.skeleton.SkeletonFrameLayout

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class RecyclerViewForm: NodeView<RecyclerViewForm.Holder>() {
    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val recyclerView = view.findViewById<RecyclerView?>(R.id.recyclerview_form_view)?.apply {
            onBindRecyclerView(context, this)
        }
        val emptyView: FrameLayout? = view.findViewById(R.id.recyclerview_form_empty)
        val loadView: FrameLayout? = view.findViewById(R.id.recyclerview_form_load)
        val loadingViewContainer: FrameLayout? = view.findViewById(R.id.recyclerview_form_loading_container)
        var loadingView: View? = view.findViewById(R.id.recyclerview_form_loading)

        init {
            ViewUtil.setProtectTouchLowLayer(loadingViewContainer, true)
        }
    }

    override fun onLayout(): Int = R.layout.recyclerview_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    abstract fun onBindRecyclerView(context: Context, recyclerView: RecyclerView)

    fun scrollToPosition(position: Int, offset: Int? = null, smoothScroll: Boolean = false) {
        val recyclerView = holder?.recyclerView ?: return
        recyclerView.post {
            if (offset != null) {
                when (val layoutManager = recyclerView.layoutManager) {
                    is LinearLayoutManager -> {
                        layoutManager.scrollToPositionWithOffset(position, offset)
                        return@post
                    }
                    is StaggeredGridLayoutManager -> {
                        layoutManager.scrollToPositionWithOffset(position, offset)
                        return@post
                    }
                    else -> {}
                }
            }

            if (smoothScroll) {
                recyclerView.smoothScrollToPosition(position)
            } else {
                recyclerView.scrollToPosition(position)
            }
        }
    }

    fun setEmptyView(view: View?) {
        if (view == null) {
            holder?.recyclerView?.visibility = View.VISIBLE
            holder?.emptyView?.visibility = View.GONE
        } else {
            holder?.recyclerView?.visibility = View.INVISIBLE
            holder?.emptyView?.apply {
                if (getChildAt(0) != view) {
                    removeAllViews()
                    addView(view)
                }
                visibility = View.VISIBLE
            }
        }
    }

    fun getEmptyParentView() = holder?.emptyView

    fun setLoadView(view: View?) {
        if (view == null) {
            holder?.recyclerView?.visibility = View.VISIBLE
            holder?.loadView?.visibility = View.INVISIBLE
        } else {
            holder?.emptyView?.visibility = View.GONE
            holder?.recyclerView?.visibility = View.INVISIBLE
            holder?.loadView?.apply {
                if (getChildAt(0) != view) {
                    removeAllViews()
                    addView(view)
                }
                visibility = View.VISIBLE
            }
        }
    }

    fun getLoadView() : View? {
        if (0 < (holder?.loadView?.childCount ?: 0)) {
            return holder?.loadView?.getChildAt(0)
        }
        return null
    }

    fun getLoadParentView() = holder?.loadView

    fun bindLoadingView(context: Context, @LayoutRes layoutId: Int) {
        bindLoadingView(ViewUtil.inflate(context, layoutId))
    }

    fun bindLoadingView(view: View?) {
        holder?.loadingViewContainer?.let { container ->
            container.removeAllViews()
            if (view != null) {
                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            holder?.loadingView = view
        }
    }

    fun showLoading() {
        holder?.loadingViewContainer?.isVisible = true
        holder?.loadingView?.let { view ->
            when (view) {
                is SkeletonFrameLayout -> {
                    view.startEffect()
                }
            }
        }
    }

    fun hideLoading() {
        val loadingContainerView = holder?.loadingViewContainer

        holder?.loadingView?.let { view ->
            when (view) {
                is SkeletonFrameLayout -> {
                    view.stopEffect()
                    loadingContainerView?.isVisible = false
                }
                else -> {
                    loadingContainerView?.isVisible = false
                }
            }
        }
    }

    fun setVisibility(visibility: Int) {
        holder?.view?.visibility = visibility
    }

    fun setVisible(isVisible: Boolean) {
        holder?.view?.isVisible = isVisible
    }

    fun getLayoutManager(): RecyclerView.LayoutManager? = holder?.recyclerView?.layoutManager

    fun onSaveInstanceState(): Parcelable? = getLayoutManager()?.onSaveInstanceState()

    fun onRestoreInstanceState(state: Parcelable?) {
        getLayoutManager()?.onRestoreInstanceState(state)
    }

    fun setChangeLayoutManager(layoutManager: RecyclerView.LayoutManager?, onComplete: (() -> Unit)? = null) {
        val recyclerView = holder?.recyclerView ?: return

        if (layoutManager == null || layoutManager == recyclerView.layoutManager) {
            return
        }
        onChangeLayoutManager(recyclerView, layoutManager, onComplete)
    }

    protected open fun onChangeLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager, onComplete: (() -> Unit)?) {}

    fun getRecyclerView(): RecyclerView? = holder?.recyclerView
}