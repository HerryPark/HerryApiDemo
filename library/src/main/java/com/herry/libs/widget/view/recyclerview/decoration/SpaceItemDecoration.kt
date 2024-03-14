package com.herry.libs.widget.view.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.herry.libs.widget.extension.setViewPadding

@Suppress("MemberVisibilityCanBePrivate")
class SpaceItemDecoration(@Px private val space: Int) : RecyclerView.ItemDecoration() {

    private val halfSpace: Int = space / 2

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val viewPosition = parent.getChildAdapterPosition(view)
        @IntRange(from = 0, to = 1) val orientation: Int
        val isInverse: Boolean
        val spanCount: Int
        val spanIndex: Int
        val isFullSpan: Boolean

        val layoutManager = parent.layoutManager
        val viewLayoutParams = view.layoutParams
        when (layoutManager) {
            is StaggeredGridLayoutManager -> {
                orientation = layoutManager.orientation
                isInverse = layoutManager.reverseLayout
                spanCount = layoutManager.spanCount
                spanIndex = (viewLayoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
                isFullSpan = viewLayoutParams.isFullSpan
            }

            is GridLayoutManager -> {
                orientation = layoutManager.orientation
                isInverse = layoutManager.reverseLayout
                spanCount = layoutManager.spanCount
                spanIndex = (viewLayoutParams as GridLayoutManager.LayoutParams).spanIndex
                isFullSpan = layoutManager.spanSizeLookup.getSpanSize(viewPosition) == spanCount
            }

            is LinearLayoutManager -> {
                orientation = layoutManager.orientation
                isInverse = layoutManager.reverseLayout
                spanCount = 1
                spanIndex = 0
                isFullSpan = true
            }

            else -> return
        }

        if (spanCount <= 0) return

        when {
            isFullSpan -> {
                outRect.top = space
                outRect.bottom = space
                outRect.left = space
                outRect.right = space
            }
            spanIndex == 0 -> {
                if (orientation == OrientationHelper.VERTICAL) {

                }
                outRect.top = halfSpace
                outRect.bottom = halfSpace
                outRect.left = space
                outRect.right = halfSpace
            }
            spanIndex == spanCount - 1 -> {
                outRect.top = halfSpace
                outRect.bottom = halfSpace
                outRect.left = halfSpace
                outRect.right = space
            }
            else -> {
                outRect.top = halfSpace
                outRect.bottom = halfSpace
                outRect.left = halfSpace
                outRect.right = halfSpace
            }
        }
    }

    fun setParentPaddingWithoutClip(parent: RecyclerView, @Px padding: Int) {
        setParentPaddingWithoutClip(parent, Rect(padding, padding, padding, padding))
    }

    fun setParentPaddingWithoutClip(parent: RecyclerView, paddings: Rect) {
        parent.clipToPadding = false
        parent.setViewPadding(paddings = paddings)
    }
}