package com.herry.test.app.intent.scheme

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup

/**
 * Created by herry.park on 2020/06/11.
 **/
class SchemePresenter : SchemeContract.Presenter() {
    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: SchemeContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: SchemeContract.View, state: ResumeState) {
        if (state == ResumeState.LAUNCH) {
//        setTestItems()
            load()
        }
    }

    private fun load() {
        val testSchemes: MutableList<SchemeContract.SchemaData> = mutableListOf(
            SchemeContract.SchemaData("App lunch", "kinemaster://kinemaster", "https://kinema.link/page/GUDU"),
            SchemeContract.SchemaData("My Information", "kinemaster://kinemaster/kmprch", ""),
            SchemeContract.SchemaData("Empty Editing page", "kinemaster://kinemaster/kmproject", "https://kinema.link/page/k29C"),
            SchemeContract.SchemaData("Asset store main", "kinemaster://kinemaster/kmasset/asset ", "https://kinema.link/page/enT8"),
            SchemeContract.SchemaData("Detail asset", "kinemaster://kinemaster/kmasset/asset/2561", "https://kinema.link/page/szAx"),
            SchemeContract.SchemaData("Unpublished Asset Item", "kinemaster://kinemaster/kmasset/asset/2931", "https://kinema.link/page/Hejk "),
            SchemeContract.SchemaData("Asset store category", "kinemaster://kinemaster/kmasset/category/2", "https://kinema.link/page/nPwh"),
            SchemeContract.SchemaData("Asset store category new", "kinemaster://kinemaster/kmasset/category/new", "https://kinema.link/page/enT8"),
            SchemeContract.SchemaData("Asset store category, sub category", "kinemaster://kinemaster/kmasset/category/1/subcategory/35", "https://kinema.link/page/bwJu"),
            SchemeContract.SchemaData("Subscription", "kinemaster://kinemaster/subscribe", "https://kinema.link/page/vQci"),
            SchemeContract.SchemaData("Subscription with  sku ID", "kinemaster://kinemaster/subscribe?sku_monthly=1&sku_annual=1", "https://kinema.link/page/1B33"),
            SchemeContract.SchemaData("Notice ", "kinemaster://kinemaster/notice", "https://kinema.link/page/b1zV"),
            SchemeContract.SchemaData("Detail Notice page", "kinemaster://kinemaster/notice/619b57583787a72405552015", "https://kinema.link/page/s5L2"),
            SchemeContract.SchemaData("Project Feed Main page", "kinemaster://kinemaster/projectfeed", "https://kinema.link/page/zEih"),
            SchemeContract.SchemaData("Detail Project Feed ", "kinemaster://kinemaster/projectfeed/61fb33a130535402f7a7dcf0 ", "https://kinema.link/page/w8m1"),
            SchemeContract.SchemaData("Detail Project Feed > Comment", "kinemaster://kinemaster/projectfeed/604def9401071402c972bb2e/comment/245 ", ""),
            SchemeContract.SchemaData("Project Feed category", "kinemaster://kinemaster/projectfeed/category/5fbcff7150ab1428a91ee16f", "https://kinema.link/page/DheA"),
            SchemeContract.SchemaData("Project Keyword search result", "kinemaster://kinemaster/projectfeed/search/background", "https://kinema.link/page/7Q5L"),
            SchemeContract.SchemaData("Me", "kinemaster://kinemaster/me", ""),
            SchemeContract.SchemaData("Me > Edit Profile", "kinemaster://kinemaster/me/profile", ""),
            SchemeContract.SchemaData("Me > Mix", "kinemaster://kinemaster/me/mix", ""),
            SchemeContract.SchemaData("Me > Follow", "kinemaster://kinemaster/me/follower", ""),
            SchemeContract.SchemaData("User (Me)", "kinemaster://kinemaster/user/395", ""),
            SchemeContract.SchemaData("User (Me) > Mix", "kinemaster://kinemaster/user/395/mix", ""),
            SchemeContract.SchemaData("User (Me) > Follow", "kinemaster://kinemaster/user/395/follower", ""),
            SchemeContract.SchemaData("User", "kinemaster://kinemaster/user/245", ""),
            SchemeContract.SchemaData("User > Mix", "kinemaster://kinemaster/user/245/mix", ""),
            SchemeContract.SchemaData("User > Follow", "kinemaster://kinemaster/user/245/follower", ""),
        )

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *testSchemes.toTypedArray())
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    private fun setTestItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        SchemeContract.SchemeItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun gotoScheme(type: SchemeContract.SchemeItemType) {
        view?.getViewContext() ?: return

        view?.onGotoScheme(type.url)
    }

    override fun gotoScheme(url: String) {
        view?.getViewContext() ?: return

        if (url.isNotEmpty()) {
            view?.onGotoScheme(url)
        }
    }
}