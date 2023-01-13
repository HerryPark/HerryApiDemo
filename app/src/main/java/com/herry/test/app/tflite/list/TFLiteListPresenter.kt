package com.herry.test.app.tflite.list

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import io.reactivex.Observable

class TFLiteListPresenter : TFLiteListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: TFLiteListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: TFLiteListContract.View, state: ResumeState) {
        if (state == ResumeState.LAUNCH) {
            loadItems()
        }
    }

    private fun loadItems() {
        subscribeObservable(
            observable = Observable.fromCallable {
                TFLiteListContract.Item.values().toMutableList()
            },
            onNext = {
                displayItems(it)
            }
        )
    }

    private fun displayItems(items: MutableList<TFLiteListContract.Item>) {
        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *items.toTypedArray())
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }
}