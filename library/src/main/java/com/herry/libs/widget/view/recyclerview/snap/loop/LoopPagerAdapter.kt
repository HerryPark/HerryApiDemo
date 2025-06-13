package com.herry.libs.widget.view.recyclerview.snap.loop

interface LoopPagerAdapter {
    fun getRealItemCount(): Int

    fun getRealPosition(fakePosition: Int): Int

    fun getFakePosition(realPosition: Int): Int
}