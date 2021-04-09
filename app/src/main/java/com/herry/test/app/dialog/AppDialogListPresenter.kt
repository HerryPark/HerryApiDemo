package com.herry.test.app.dialog

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

/**
 * Created by herry.park on 2020/06/11.
 **/
class AppDialogListPresenter : AppDialogListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: AppDialogListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: AppDialogListContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        // sets list items
        setTestItems()
    }

    private fun setTestItems() {
        view?.getContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        AppDialogListContract.TestItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upSert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun moveToScreen(type: AppDialogListContract.TestItemType) {
        view?.onScreen(type)
    }
}