package com.herry.test.app.list.fastscroller

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

class FastScrollerListPresenter : FastScrollerListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: FastScrollerListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: FastScrollerListContract.View, state: ResumeState) {
        if (state == ResumeState.LAUNCH) {
            loadList()
        }
    }

    private fun loadList() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

        NodeHelper.addModels(nodes, *getAlphabetItems('A', 'Z').toTypedArray())
        NodeHelper.addModels(nodes, *getAlphabetItems('a', 'z').toTypedArray())

        NodeHelper.upsert(this.nodes, nodes)

        this.nodes.endTransition()
    }


    private fun getAlphabetItems(start: Char, end: Char) : MutableList<FastScrollerListContract.ListItemData> {
        val items = mutableListOf<FastScrollerListContract.ListItemData>()
        (start .. end).forEach { ch ->
            items.add(FastScrollerListContract.ListItemData("$ch"))
        }

        return items
    }
}