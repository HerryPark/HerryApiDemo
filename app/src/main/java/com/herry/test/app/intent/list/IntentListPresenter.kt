package com.herry.test.app.intent.list

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

/**
 * Created by herry.park on 2020/06/11.
 **/
class IntentListPresenter : IntentListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: IntentListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: IntentListContract.View, state: ResumeState) {
        if (state == ResumeState.LAUNCH) {
            // sets list items
            setTestItems()
        }
    }

    private fun setTestItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        IntentListContract.TestItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun moveToScreen(type: IntentListContract.TestItemType) {
        view?.onScreen(type)
    }
}