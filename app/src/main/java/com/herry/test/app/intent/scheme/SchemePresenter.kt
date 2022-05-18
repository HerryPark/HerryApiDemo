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

    override fun onLaunch(view: SchemeContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        // sets list items
//        setTestItems()
        load()
    }

    private fun load() {
        val testSchemes: MutableList<SchemeContract.SchemaData> = mutableListOf(
            SchemeContract.SchemaData("App lunch", "kinemaster://kinemaster", "https://kinema.link/page/GUDU"),
            SchemeContract.SchemaData("My Information", "kinemaster://kinemaster/kmprch", ""),
            SchemeContract.SchemaData("Empty Editing page", "kinemaster://kinemaster/kmproject", "https://kinema.link/page/k29C"),
            SchemeContract.SchemaData("Asset store main", "kinemaster://kinemaster/kmasset/asset ", "https://kinema.link/page/enT8"),
            SchemeContract.SchemaData("Detail asset", "kinemaster://kinemaster/kmasset/asset", "https://kinema.link/page/5wHg"),
            SchemeContract.SchemaData("Asset store  category", "kinemaster://kinemaster/kmasset/category/2", "https://kinema.link/page/nPwh"),
            SchemeContract.SchemaData("Asset store  category,  sub category", "kinemaster://kinemaster/kmasset/category/1/subcategory/12", "https://kinema.link/page/rY6Y"),
            SchemeContract.SchemaData("Subscription", "kinemaster://kinemaster/subscribe", "https://kinema.link/page/vQci"),
            SchemeContract.SchemaData("Subscription  with  sku ID", "kinemaster://kinemaster/subscribe?sku_monthly=1&sku_annual=1", "https://kinema.link/page/1B33"),
            SchemeContract.SchemaData("Notice ", "kinemaster://kinemaster/notice", "https://kinema.link/page/b1zV"),
            SchemeContract.SchemaData("Detail Notice page", "kinemaster://kinemaster/notice/619b57583787a72405552015", "https://kinema.link/page/s5L2"),
            SchemeContract.SchemaData("Project Feed Main page", "kinemaster://kinemaster/projectfeed", "https://kinema.link/page/zEih"),
            SchemeContract.SchemaData("Detail Project Feed ", "kinemaster://kinemaster/projectfeed/61fb33a130535402f7a7dcf0 ", "https://kinema.link/page/w8m1"),
            SchemeContract.SchemaData("Project Feed category", "kinemaster://kinemaster/projectfeed/category/5fbcff7150ab1428a91ee16f", "https://kinema.link/page/DheA"),
            SchemeContract.SchemaData("Project Keyword search result", "kinemaster://kinemaster/projectfeed/search/background", "https://kinema.link/page/7Q5L"),
            SchemeContract.SchemaData("Mix", "", ""),
            SchemeContract.SchemaData("Mix Detail Feed", "", ""),
            SchemeContract.SchemaData("Me", "", ""),
            SchemeContract.SchemaData("Create", "", ""),
        )

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *testSchemes.toTypedArray())
        NodeHelper.upSert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    private fun setTestItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        SchemeContract.SchemeItemType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upSert(this.nodes, nodes)
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