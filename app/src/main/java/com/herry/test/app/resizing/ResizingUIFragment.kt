package com.herry.test.app.resizing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.widget.extension.setImage
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView

class ResizingUIFragment: BaseMVPNavView<ResizingUIContract.View, ResizingUIContract.Presenter>(), ResizingUIContract.View {
    override fun onCreatePresenter(): ResizingUIContract.Presenter = ResizingUIPresenter()

    override fun onCreatePresenterView(): ResizingUIContract.View = this

    private var container: View? = null

    override val root: NodeRoot
        get() = adapter.root

    private val adapter: Adapter = Adapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.resizing_ui_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        view.findViewById<RecyclerView>(R.id.resizing_ui_fragment_menus)?.apply {
            layoutManager = GridLayoutManager(context, 3)
            setHasFixedSize(true)
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            adapter = this@ResizingUIFragment.adapter
        }
    }

    inner class Adapter: NodeRecyclerAdapter(::requireContext) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(MenuItemForm())
        }
    }

    private class MenuItemForm: NodeForm<MenuItemForm.Holder, ResizingUIContract.MenuItemModel>(Holder::class, ResizingUIContract.MenuItemModel::class) {
        inner class Holder(context: Context, view: View): NodeHolder(context, view) {
            val icon: ImageView? = view.findViewById(R.id.resizing_ui_menu_item_form_icon)
        }

        override fun onLayout(): Int = R.layout.resizing_ui_menu_item_form

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: ResizingUIContract.MenuItemModel) {
            holder.icon?.setImage(model.icon, R.color.selector_icon, R.color.selector_icon)
        }
    }

}