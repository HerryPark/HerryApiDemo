package com.herry.libs.widget.view.dialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.herry.libs.R
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setImage
import com.herry.libs.widget.extension.setOnProtectClickListener
import java.io.Serializable

open class BottomSheetListDialog
@Throws(IllegalAccessException::class) constructor(
    activity: Activity?,
    @StyleRes themeResId: Int = R.style.Theme_Design_BottomSheetDialog,
    attributes: Attributes? = null,
    private val bindForms: MutableList<NodeForm<out NodeHolder, *>> = mutableListOf()
) {
    private val context: ContextThemeWrapper

    private val dialog: BottomSheetDialog

    private val node: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    private val view: View

    private var recyclerView: RecyclerView? = null

    private val adapter: Adapter = Adapter()

    data class Attributes(
        val title: String = "",
        val direction: Int = RecyclerView.VERTICAL,
        val maxHeight: Int = WindowManager.LayoutParams.MATCH_PARENT,
        val background: Drawable? = null,
        val paddingStart: Int = 0,
        val paddingEnd: Int = 0,
        val paddingTop: Int = 0,
        val paddingBottom: Int = 0
    )

    init {
        this.context = if (activity != null) ContextThemeWrapper(activity, themeResId) else throw IllegalAccessException()
        this.view = ViewUtil.inflate(context, R.layout.bottom_sheet_list_dialog)
        this.dialog = BottomSheetDialog(context, themeResId).apply {
            window?.setBackgroundDrawable(null)
        }
        init(this.view, attributes)
    }

    private fun init(view: View, attributes: Attributes?) {
        val context = view.context ?: return

        view.background = attributes?.background ?: ViewUtil.getDrawable(context, R.drawable.bg_bottom_sheet_dialog)

        view.findViewById<TextView>(R.id.bottom_sheet_list_dialog_title)?.run {
            this.text = attributes?.title ?: ""
            this.isVisible = text.isNotEmpty()
        }

        this.recyclerView = view.findViewById<RecyclerView?>(R.id.bottom_sheet_list_dialog_recycler_view)?.apply {
            val direction = attributes?.direction ?: RecyclerView.VERTICAL
            this.layoutManager = LinearLayoutManager(context, direction, false)
            setHasFixedSize(true)
            if (this.itemAnimator is SimpleItemAnimator) {
                (this.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            this.itemAnimator = null
            this.adapter = this@BottomSheetListDialog.adapter
        }

        this.adapter.root.beginTransition()
        NodeHelper.addNode(this.adapter.root, this.node)
        this.adapter.root.endTransition()

        this.dialog.setContentView(view)

        val maxHeight = attributes?.maxHeight ?: WindowManager.LayoutParams.MATCH_PARENT
        setDialogHeight(this.dialog, maxHeight)
    }

    private fun setDialogHeight(dialog: BottomSheetDialog, height: Int) {
        val window: Window = dialog.window ?: return

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window.attributes)
        val layoutParams: ViewGroup.LayoutParams = view.layoutParams
        layoutParams.height = height
        view.layoutParams = layoutParams

        // This makes the dialog take up the full width
        lp.width = layoutParams.width
        lp.height = height
        window.attributes = lp
    }

    open fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {}

    private inner class Adapter: NodeRecyclerAdapter(::context) {
        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            bindForms.forEach {
                list.add(it)
            }
            this@BottomSheetListDialog.onBindForms(list)
        }
    }

    fun setItems(items: MutableList<*>) {
        this.node.beginTransition()

        val node = NodeHelper.createNodeGroup()
        items.filterNotNull().forEach { item ->
            NodeHelper.addModel(node, item)
        }
        NodeHelper.upsert(this.node, node)

        this.node.endTransition()
    }

    fun scrollTo(position: Int) {
        if (0 <= position && position < node.getChildCount()) {
            recyclerView?.scrollToPosition(position)
        }
    }

    fun isShowing(): Boolean = dialog.isShowing

    fun show(type: ShowType = ShowType.EXPANDED) {
        if (!dialog.isShowing) {
            dialog.setOnShowListener {
                this.dialog.behavior.let { behavior ->
                    behavior.skipCollapsed = type == ShowType.EXPANDED
                    behavior.state = when (type) {
                        ShowType.COLLAPSED -> BottomSheetBehavior.STATE_COLLAPSED
                        ShowType.EXPANDED -> BottomSheetBehavior.STATE_EXPANDED
                        ShowType.HALF_EXPANDED -> BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                }
            }
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    enum class ShowType {
        COLLAPSED,
        EXPANDED,
        HALF_EXPANDED,
    }
}

@Suppress("unused")
class BottomSheetListItemForm(
    private val onClick: (which: Model) -> Unit
) : NodeForm<BottomSheetListItemForm.Holder, BottomSheetListItemForm.Model>(Holder::class, Model::class) {
    data class Model(
        val text: String
    ) : Serializable

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val text: TextView? = view.findViewById(R.id.bottom_sheet_list_item_text)
        val subText: TextView? = view.findViewById(R.id.bottom_sheet_list_item_sub_text)
        val icon: ImageView? = view.findViewById(R.id.bottom_sheet_list_item_icon)

        init {
            view.setOnProtectClickListener {
                NodeRecyclerForm.getBindNode(this@BottomSheetListItemForm, this@Holder)?.let { node ->
                    onClick(node.model)
                }
            }
        }
    }

    override fun onLayout(): Int = R.layout.bottom_sheet_list_item

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.icon?.isVisible = false
        holder.text?.text = model.text
        holder.subText?.isVisible = false
    }
}

class BottomSheetListSingleChoiceItemForm(
    private val onClick: (which: Model) -> Unit
) : NodeForm<BottomSheetListSingleChoiceItemForm.Holder, BottomSheetListSingleChoiceItemForm.Model>(
    Holder::class, Model::class) {
    data class Model(
        val text: String,
        val subText: String = "",
        val selected: Boolean,
        val enabled: Boolean = true,
        val data: Any
    ) : Serializable

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val text: TextView? = view.findViewById(R.id.bottom_sheet_list_item_text)
        val subText: TextView? = view.findViewById(R.id.bottom_sheet_list_item_sub_text)
        val icon: ImageView? = view.findViewById(R.id.bottom_sheet_list_item_icon)

        init {
            view.setOnProtectClickListener {
                NodeRecyclerForm.getBindNode(this@BottomSheetListSingleChoiceItemForm, this@Holder)?.let { node ->
                    onClick(node.model)
                }
            }
        }
    }

    override fun onLayout(): Int = R.layout.bottom_sheet_list_item

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.icon?.setImage(when {
            model.selected && model.enabled -> R.drawable.btn_check_on
            model.selected && !model.enabled -> R.drawable.btn_check_on_disabled
            !model.selected && model.enabled -> R.drawable.btn_check_off
            else -> R.drawable.btn_check_off_disabled /*!model.selected && !model.enabled*/
        })
        holder.text?.text = model.text
        holder.subText?.let { view ->
            view.text = model.subText
            view.isVisible = model.subText.isNotEmpty()
        }
        holder.view.isSelected = model.selected

        ViewUtil.setViewGroupEnabled(holder.view, model.enabled)
    }
}