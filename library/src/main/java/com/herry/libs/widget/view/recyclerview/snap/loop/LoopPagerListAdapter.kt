package com.herry.libs.widget.view.recyclerview.snap.loop

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class LoopPagerListAdapter<VM, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<VM>) : ListAdapter<VM, VH>(diffCallback), LoopPagerAdapter {
    private var maxItemCounts: Int = 0
    private var defaultPosition: Int = RecyclerView.NO_POSITION

    override fun submitList(list: List<VM?>?) {
        super.submitList(list)

        val itemCounts = getRealItemCount()
        if (itemCounts > 1) {
            maxItemCounts = Int.MAX_VALUE
            defaultPosition = maxItemCounts / 2 - maxItemCounts / 2 % itemCounts
        } else if (itemCounts > 0) {
            maxItemCounts = itemCounts
            defaultPosition = 0
        } else {
            maxItemCounts = itemCounts
        }
    }

    override fun getItem(position: Int): VM? {
        val realPosition = getRealPosition(position)
        val realItemCount = getRealItemCount()
        return if (0 <= realPosition && realPosition < realItemCount) {
            super.getItem(realPosition)
        } else null
    }

    override fun getItemCount(): Int = maxItemCounts

    override fun getRealItemCount(): Int = super.getItemCount()

    override fun getRealPosition(fakePosition: Int): Int {
        val realItemCount = getRealItemCount()
        return if (1 < realItemCount) fakePosition % realItemCount else fakePosition
    }

    override fun getFakePosition(realPosition: Int): Int {
        val realItemCount = getRealItemCount()
        return if (0 <= realPosition && realPosition < realItemCount) {
            defaultPosition + realPosition
        } else RecyclerView.NO_POSITION
    }
}