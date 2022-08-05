package com.herry.libs.widget.view.recyclerview.form.recycler

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.herry.libs.R
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.NodeView
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.view.skeleton.SkeletonFrameLayout
import com.herry.libs.widget.view.viewgroup.LoadingCountView

@Suppress("unused")
abstract class RecyclerForm : NodeView<RecyclerForm.Holder>() {

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val recyclerView: RecyclerView? = view.findViewById<RecyclerView?>(R.id.recyclerview_form_view)?.apply {
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

    override fun onLayout(): Int = R.layout.recycler_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    abstract fun onBindRecyclerView(context: Context, recyclerView: RecyclerView)

    fun scrollToPosition(position: Int, offset: Int? = null, smoothScroll: Boolean = false) {
        offset?.let {
            when (val layoutManager = holder?.recyclerView?.layoutManager) {
                is LinearLayoutManager -> {
                    layoutManager.scrollToPositionWithOffset(position, offset)
                    return
                }
                is GridLayoutManager -> {
                    layoutManager.scrollToPositionWithOffset(position, offset)
                    return
                }
                is StaggeredGridLayoutManager -> {
                    layoutManager.scrollToPositionWithOffset(position, offset)
                    return
                }
                else -> {}
            }
        }

        holder?.recyclerView?.run {
            if (smoothScroll) smoothScrollToPosition(position) else scrollToPosition(position)
        }
    }

    fun smoothScrollToPosition(position: Int) {
        holder?.recyclerView?.smoothScrollToPosition(position)
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
                is LoadingCountView -> view.show()
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
                is LoadingCountView -> view.hide(object: LoadingCountView.OnHideListener {
                    override fun onDone() {
                        loadingContainerView?.isVisible = false
                    }
                })
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

    fun isVisible(isVisible: Boolean) {
        holder?.view?.isVisible = isVisible
    }
}