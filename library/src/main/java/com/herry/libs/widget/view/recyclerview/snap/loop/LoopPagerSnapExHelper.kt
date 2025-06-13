package com.herry.libs.widget.view.recyclerview.snap.loop

import androidx.recyclerview.widget.RecyclerView
import com.herry.libs.widget.view.recyclerview.snap.PagerSnapExHelper

open class LoopPagerSnapExHelper : PagerSnapExHelper() {

    private var snappedRealPosition = RecyclerView.NO_POSITION
    private var snappedFakeViewPosition: Int = RecyclerView.NO_POSITION

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val recyclerView = super.getRecyclerView() ?: return RecyclerView.NO_POSITION

        val adapter = recyclerView.adapter ?: return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (adapter !is LoopPagerRecyclerViewAdapter<*,*>) {
            return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        }

        val targetSnapViewPosition = findTargetSnapViewPosition(layoutManager, velocityX, velocityY)
        if (targetSnapViewPosition >= 0 && targetSnapViewPosition < adapter.getItemCount()) {
            val targetSnapRealPosition = adapter.getRealPosition(targetSnapViewPosition)
            val totalRealItemCounts = adapter.getRealItemCount()
            if (this.snappedRealPosition != targetSnapRealPosition) {
                val unsnappedPosition = this.snappedRealPosition
                this.snappedRealPosition = targetSnapRealPosition
                notifySnapped(unsnappedPosition, this.snappedRealPosition, totalRealItemCounts, targetSnapViewPosition)
            }
        }
        return targetSnapViewPosition
    }

    override fun scrollToSnapPosition(position: Int, force: Boolean) {
        val recyclerView: RecyclerView = getRecyclerView() ?: return
        val adapter = recyclerView.adapter ?: run {
            super.scrollToSnapPosition(position, force)
            return@scrollToSnapPosition
        }

        if (adapter !is LoopPagerRecyclerViewAdapter<*, *>) {
            super.scrollToSnapPosition(position, force)
            return
        }

        val layoutManager: RecyclerView.LayoutManager? = recyclerView.layoutManager
        val onSnappedListener = getOnSnappedListener()

        if (null != layoutManager && null != onSnappedListener) {
            val snapViewPosition = findTargetSnapViewPosition(layoutManager, recyclerView.computeHorizontalScrollOffset(), recyclerView.computeVerticalScrollOffset())
            val fakePosition = adapter.getFakePosition(position)
            if (snapViewPosition != fakePosition) {
                val totalRealItemCounts = adapter.getRealItemCount()
                layoutManager.scrollToPosition(fakePosition)

                var unsnappedPosition: Int = RecyclerView.NO_POSITION
                if (this.snappedRealPosition >= 0) {
                    unsnappedPosition = this.snappedRealPosition
                }
                this.snappedRealPosition = position
                notifySnapped(unsnappedPosition, this.snappedRealPosition, totalRealItemCounts, fakePosition)
            }
        }
    }

    override fun getCurrentSnappedPosition(): Int {
        return snappedRealPosition
    }

    protected open fun notifySnapped(unsnappedPosition: Int, snappedPosition: Int, itemCounts: Int, snappedFakePosition: Int) {
        snappedFakeViewPosition = snappedFakePosition
        super.notifySnapped(unsnappedPosition, snappedPosition, itemCounts)
    }

    override fun snapToNext() {
        val recyclerView = getRecyclerView() ?: return
        val adapter = recyclerView.adapter ?: run {
            super.snapToNext()
            return@snapToNext
        }

        if (adapter !is LoopPagerAdapter) {
            super.snapToNext()
            return
        }

        if (isScrollable()) {
            val fakeViewPosition = this.snappedFakeViewPosition
            if (fakeViewPosition == RecyclerView.NO_POSITION || fakeViewPosition >= adapter.itemCount - 1) {
                scrollToSnapPosition(0)
                return
            }

            val unsnappedPosition = adapter.getRealPosition(fakeViewPosition)
            val snappedPosition = adapter.getRealPosition(fakeViewPosition + 1)

            recyclerView.smoothScrollToPosition(fakeViewPosition + 1)
            notifySnapped(unsnappedPosition, snappedPosition, adapter.getRealItemCount(), fakeViewPosition + 1)
            this.snappedRealPosition = snappedPosition
            this.snappedFakeViewPosition = fakeViewPosition + 1
        }
    }

    override fun snapToPrevious() {
        val recyclerView = getRecyclerView() ?: return
        val adapter = recyclerView.adapter ?: run {
            super.snapToPrevious()
            return@snapToPrevious
        }

        if (adapter !is LoopPagerRecyclerViewAdapter<*, *>) {
            super.snapToPrevious()
            return
        }

        if (isScrollable()) {
            val fakeViewPosition = this.snappedFakeViewPosition
            if (fakeViewPosition == RecyclerView.NO_POSITION || fakeViewPosition <= 0) {
                scrollToSnapPosition(adapter.getRealItemCount() - 1)
                return
            }
            val unsnappedPosition = adapter.getRealPosition(fakeViewPosition)
            val snappedPosition = adapter.getRealPosition(fakeViewPosition - 1)
            recyclerView.smoothScrollToPosition(fakeViewPosition - 1)
            notifySnapped(unsnappedPosition, snappedPosition, adapter.getRealItemCount(), fakeViewPosition - 1)
            this.snappedRealPosition = snappedPosition
            this.snappedFakeViewPosition = fakeViewPosition - 1
        }
    }
}