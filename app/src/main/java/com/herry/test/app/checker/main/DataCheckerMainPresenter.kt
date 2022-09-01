package com.herry.test.app.checker.main

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.util.preferences.PreferenceHelper
import com.herry.test.sharedpref.SharedPrefKeys

/**
 * Created by herry.park on 2020/7/7
 **/
class DataCheckerMainPresenter : DataCheckerMainContract.Presenter() {
    private val passwordNodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: DataCheckerMainContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, passwordNodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: DataCheckerMainContract.View, recreated: Boolean) {
        // sets list items
        display()
    }

    override fun refresh() {
        display()
    }

    private fun display() {
        view?.getViewContext() ?: return

        displayPassword()
    }

    private fun displayPassword() {
        this.passwordNodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModel(nodes, DataCheckerMainContract.PasswordModel(
            PreferenceHelper.get(SharedPrefKeys.PASSWORD, "")
        ))
        NodeHelper.upsert(this.passwordNodes, nodes)
        this.passwordNodes.endTransition()
    }
}