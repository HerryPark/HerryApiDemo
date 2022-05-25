package com.herry.test.app.main

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

/**
 * Created by herry.park on 2020/06/11.
 **/
class MainPresenter : MainContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: MainContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: MainContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        // sets list items
        setTestItems()
    }

    private fun setTestItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        MainContract.TestItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upSert(this.nodes, nodes)
        this.nodes.endTransition()
    }

//    private fun throttleFirst() {
//        subscribeObservable(
//            Observable.create<Int> { emitter ->
//                for (i in 0..9) {
//                    emitter.onNext(i)
//                    try {
//                        Thread.sleep(100)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                }
//                emitter.onComplete()
//            }.throttleFirst(200, TimeUnit.MILLISECONDS)
//        , {
//                Trace.d("Herry", "throttle: $it")
//            }
//        )
//    }

    override fun moveToScreen(type: MainContract.TestItemType) {
        view?.onScreen(type)
    }
}