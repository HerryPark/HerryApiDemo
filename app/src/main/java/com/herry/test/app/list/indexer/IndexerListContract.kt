package com.herry.test.app.list.indexer

import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter

interface IndexerListContract {
    interface View : MVPView<Presenter>, INodeRoot

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun getSections(): MutableList<String>
        abstract fun getPositionForSection(sectionIndex: Int): Int
        abstract fun getSectionForPosition(position: Int): Int
    }

    data class ListItemData(
        val name: String
    )
}