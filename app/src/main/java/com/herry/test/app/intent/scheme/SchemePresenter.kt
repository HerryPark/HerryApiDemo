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
            SchemeContract.SchemaData(
                title = "App Update",
                appLink = "kinemaster://kinemaster/update",
                shortLink = "https://kine.to/update"
            ),
            SchemeContract.SchemaData(
                title = "App lunch",
                appLink = "kinemaster://kinemaster",
                dynamicLink = "https://kinema.link/page/GUDU"
            ),
            SchemeContract.SchemaData(
                title = "My Information (old)",
                appLink = "kinemaster://kinemaster/kmprch",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "My Information (new)",
                appLink = "kinemaster://kinemaster/me",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Empty Editing page (old)",
                appLink = "kinemaster://kinemaster/kmproject",
                dynamicLink = "https://kinema.link/page/k29C"
            ),
            SchemeContract.SchemaData(
                title = "Empty Editing page (new)",
                appLink = "kinemaster://kinemaster/edit",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Asset store main (old)",
                appLink = "kinemaster://kinemaster/kmasset/asset",
                dynamicLink = "https://kinema.link/page/enT8"
            ),
            SchemeContract.SchemaData(
                title = "Asset store main (new)",
                appLink = "kinemaster://kinemaster/assetstore",
                shortLink = "https://kine.to/assetstore"
            ),
            SchemeContract.SchemaData(
                title = "Detail asset (old)",
                appLink = "kinemaster://kinemaster/kmasset/asset/2561",
                dynamicLink = "https://kinema.link/page/szAx"
            ),
            SchemeContract.SchemaData(
                title = "Detail asset (new)",
                appLink = "kinemaster://kinemaster/assetstore/asset/2561",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Unpublished Asset Item (old)",
                appLink = "kinemaster://kinemaster/kmasset/asset/2931",
                dynamicLink = "https://kinema.link/page/Hejk "
            ),
            SchemeContract.SchemaData(
                title = "Unpublished Asset Item (new)",
                appLink = "kinemaster://kinemaster/assetstore/asset/2931",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Asset store category (old)",
                appLink = "kinemaster://kinemaster/kmasset/category/2",
                dynamicLink = "https://kinema.link/page/nPwh"
            ),
            SchemeContract.SchemaData(
                title = "Asset store category (new)",
                appLink = "kinemaster://kinemaster/assetstore/category/2",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Asset store new category (old)",
                appLink = "kinemaster://kinemaster/kmasset/category/new",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Asset store new category (new)",
                appLink = "kinemaster://kinemaster/assetstore/new",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Asset store category, sub category (old)",
                appLink = "kinemaster://kinemaster/kmasset/category/1/subcategory/35",
                dynamicLink = "https://kinema.link/page/bwJu"
            ),
            SchemeContract.SchemaData(
                title = "Asset store category, sub category (new)",
                appLink = "kinemaster://kinemaster/assetstore/category/1/subcategory/35",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Subscription",
                appLink = "kinemaster://kinemaster/subscribe",
                dynamicLink = "https://kinema.link/page/vQci"
            ),
            SchemeContract.SchemaData(
                title = "Subscription with  sku ID",
                appLink = "kinemaster://kinemaster/subscribe?sku_monthly=1&sku_annual=1",
                dynamicLink = "https://kinema.link/page/1B33"
            ),
            SchemeContract.SchemaData(
                title = "Notice ",
                appLink = "kinemaster://kinemaster/notice",
                dynamicLink = "https://kinema.link/page/b1zV"
            ),
            SchemeContract.SchemaData(
                title = "Detail Notice page",
                appLink = "kinemaster://kinemaster/notice/619b57583787a72405552015",
                dynamicLink = "https://kinema.link/page/s5L2"
            ),
            SchemeContract.SchemaData(
                title = "Project Feed Main page (old)",
                appLink = "kinemaster://kinemaster/projectfeed",
                dynamicLink = "https://kinema.link/page/zEih"
            ),
            SchemeContract.SchemaData(
                title = "Project Feed Main page (new)",
                appLink = "kinemaster://kinemaster/template",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Detail Project Feed (old)",
                appLink = "kinemaster://kinemaster/projectfeed/61fb33a130535402f7a7dcf0 ",
                dynamicLink = "https://kinema.link/page/w8m1"
            ),
            SchemeContract.SchemaData(
                title = "Detail Project Feed (new)",
                appLink = "kinemaster://kinemaster/template/61fb33a130535402f7a7dcf0 ",
                shortLink = "https://kine.to/template/61fb33a130535402f7a7dcf0"
            ),
            SchemeContract.SchemaData(
                title = "Detail Project Feed > Comment (old)",
                appLink = "kinemaster://kinemaster/projectfeed/604def9401071402c972bb2e/comment/245 ",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Detail Project Feed > Comment (new)",
                appLink = "kinemaster://kinemaster/template/604def9401071402c972bb2e/comment/245 ",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Project Feed category (old)",
                appLink = "kinemaster://kinemaster/projectfeed/category/5fbcff7150ab1428a91ee16f",
                dynamicLink = "https://kinema.link/page/DheA"
            ),
            SchemeContract.SchemaData(
                title = "Project Feed category (new)",
                appLink = "kinemaster://kinemaster/template/category/5fbcff7150ab1428a91ee16f",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Project Keyword search result (old)",
                appLink = "kinemaster://kinemaster/projectfeed/search/background",
                dynamicLink = "https://kinema.link/page/7Q5L"
            ),
            SchemeContract.SchemaData(
                title = "Project Keyword search result (new)",
                appLink = "kinemaster://kinemaster/template/search/background",
                shortLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Inbox",
                appLink = "kinemaster://kinemaster/inbox",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Me",
                appLink = "kinemaster://kinemaster/me",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Me > Edit Profile",
                appLink = "kinemaster://kinemaster/me/profile",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Me > Mix",
                appLink = "kinemaster://kinemaster/me/mix",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "Me > Follow",
                appLink = "kinemaster://kinemaster/me/follower",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "User (Me)",
                appLink = "kinemaster://kinemaster/user/395",
                dynamicLink = "",
                shortLink = "https://kine.to/@395"
            ),
            SchemeContract.SchemaData(
                title = "User (Me) > Mix",
                appLink = "kinemaster://kinemaster/user/395/mix",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "User (Me) > Follow",
                appLink = "kinemaster://kinemaster/user/395/follower",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "User",
                appLink = "kinemaster://kinemaster/user/245",
                dynamicLink = "",
                shortLink = "https://kine.to/@8564227"
            ),
            SchemeContract.SchemaData(
                title = "User > Mix",
                appLink = "kinemaster://kinemaster/user/245/mix",
                dynamicLink = ""
            ),
            SchemeContract.SchemaData(
                title = "User > Follow",
                appLink = "kinemaster://kinemaster/user/245/follower",
                dynamicLink = ""
            ),
        )

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *testSchemes.toTypedArray())
        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    override fun gotoScheme(url: String) {
        view?.getViewContext() ?: return

        if (url.isNotEmpty()) {
            view?.onGotoScheme(url)
        }
    }
}