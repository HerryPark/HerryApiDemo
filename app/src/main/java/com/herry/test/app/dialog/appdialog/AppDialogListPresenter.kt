package com.herry.test.app.dialog.appdialog

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

    override fun onResume(view: AppDialogListContract.View, state: ResumeState) {
        if (state == ResumeState.LAUNCH) {
            // sets list items
            setTestItems()
        }
    }

    private fun setTestItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        AppDialogListContract.TestItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun moveToScreen(type: AppDialogListContract.TestItemType) {
        view?.onScreen(type)
    }
}