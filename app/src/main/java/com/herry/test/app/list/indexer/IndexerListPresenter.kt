package com.herry.test.app.list.indexer

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

class IndexerListPresenter : IndexerListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: IndexerListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: IndexerListContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        loadList()
    }

    private fun loadList() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

        NodeHelper.addModels(nodes, *getAlphabetItems('A', 'Z').toTypedArray())
        NodeHelper.addModels(nodes, *getAlphabetItems('a', 'z').toTypedArray())

//        for (i in 0 until 10) {
//            NodeHelper.addModel(nodes, IndexerListContract.ListItemData("$i"))
//        }
//        for (i in 0 until 10) {
//            NodeHelper.addModel(nodes, IndexerListContract.ListItemData("B"))
//        }

        NodeHelper.upsert(this.nodes, nodes)

        this.nodes.endTransition()
    }

    private fun getAlphabetItems(start: Char, end: Char) : MutableList<IndexerListContract.ListItemData> {
        val items = mutableListOf<IndexerListContract.ListItemData>()
        (start .. end).forEach { ch ->
            items.add(IndexerListContract.ListItemData("$ch"))
        }

        return items
    }

    private var sectionPositions: MutableList<Int> = mutableListOf()
    override fun getSections(): MutableList<String> {
        sectionPositions.clear()

        val sections: MutableList<String> = ArrayList()

        NodeHelper.getChildrenNode<IndexerListContract.ListItemData>(this.nodes).forEachIndexed { index, node ->
            if (node.model.name.isNotBlank()) {
                val section: String = node.model.name.toCharArray()[0].toString()
                if (!sections.contains(section)) {
                    sections.add(section)
                    sectionPositions.add(index)
                }
            }
        }

        return sections
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        if (sectionIndex >= 0 && sectionIndex < sectionPositions.size) {
            return sectionPositions[sectionIndex]
        }
        return 0
    }

    override fun getSectionForPosition(position: Int): Int = 0
}