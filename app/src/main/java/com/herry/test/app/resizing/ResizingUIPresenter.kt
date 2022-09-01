package com.herry.test.app.resizing

import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.R

class ResizingUIPresenter: ResizingUIContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: ResizingUIContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: ResizingUIContract.View, recreated: Boolean) {
        setMenuItems()
    }
    
    private fun setMenuItems() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_clip_graphic_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_effect_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_font_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_home_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_image_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_music_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_new_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_sound_effect_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_sticker_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_transition_enabled))
        NodeHelper.addModel(nodes, ResizingUIContract.MenuItemModel(R.drawable.ic_assetstore_sidemenu_video_enabled))

        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }
}