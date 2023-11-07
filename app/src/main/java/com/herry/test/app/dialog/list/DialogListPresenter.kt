package com.herry.test.app.dialog.list

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.app.intent.list.IntentListContract

class DialogListPresenter : DialogListContract.Presenter() {
    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: DialogListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: DialogListContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            // sets list items
            setListItems()
        }
    }

    private fun setListItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        DialogListContract.ItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun moveToScreen(type: DialogListContract.ItemType) {
        view?.onScreen(type)
    }
}