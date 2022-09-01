package com.herry.test.widget

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.libs.nodeview.recycler.NodeRecyclerAdapter
import com.herry.libs.nodeview.recycler.NodeRecyclerForm
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.libs.widget.view.recyclerview.form.LinearSnapRecyclerViewForm
import com.herry.test.R

class SingleLineChipsForm(
    @DimenRes private val padding: Int,
    @ColorRes private val backgroundColor: Int = 0,
    private val onClickChip: (chip: Chip) -> Unit
): LinearSnapRecyclerViewForm<SingleLineChipsForm.Adapter, SingleLineChipsForm.Chips>(Chips::class) {

    override fun onLayout(): Int = R.layout.single_line_chips_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(
        context,
        view,
        view.findViewById(R.id.single_line_chips_form_recycler),
        padding,
        backgroundColor
    )

    class Adapter(private val _context: Context, private val _onClickChip: (chip: Chip) -> Unit) : NodeRecyclerAdapter( {_context}) {

        private var node: Node<NodeModelGroup>? = null

        override fun onBindForms(list: MutableList<NodeForm<out NodeHolder, *>>) {
            list.add(ChipForm(object: ChipForm.OnFormListener {
                override fun onClickChip(chip: Chip) {
                    _onClickChip(chip)
                }
            }))
        }

        @Suppress("INACCESSIBLE_TYPE")
        fun setItems(list: List<Chip>) {
            setNode(super.root, list)
        }

        private fun setNode(root: NodeRoot, list: List<Chip>) {
            val node = NodeHelper.createNodeGroup()
            for (obj in list) {
                NodeHelper.addModel<Any>(node, obj)
            }

            root.beginTransition()
            this.node = NodeHelper.upsert(root, this.node, node)
            root.endTransition()
        }
    }

    override fun onGetAdapter(context: Context): Adapter = Adapter(context, onClickChip)

    override fun onBindAdapter(context: Context, adapter: Adapter, recyclerView: RecyclerView, item: Chips) {
        adapter.setItems(item.chips)
        recyclerView.scrollToPosition(0)
    }

    class Chips(val chips: List<Chip>)

    class ChipForm(val listener: OnFormListener) : NodeForm<ChipForm.Holder, Chip>(Holder::class, Chip::class) {

        interface OnFormListener {
            fun onClickChip(chip: Chip)
        }

        inner class Holder(context: Context, itemView: View): NodeHolder(context, itemView) {
            val icon: ImageView = itemView.findViewById(R.id.chip_form_icon)
            val text: TextView = itemView.findViewById(R.id.chip_form_text)

            init {
                itemView.setOnProtectClickListener {
                    val item: Chip? = NodeRecyclerForm.getBindModel(this@ChipForm, this@Holder)
                    item?.let{
                        listener.onClickChip(it)
                    }
                }
            }
        }

        override fun onLayout(): Int = R.layout.chip_form

        override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

        override fun onBindModel(context: Context, holder: Holder, model: Chip) {
            if (model.icon != 0) {
                holder.icon.isVisible = true
                holder.icon.setImageResource(model.icon)
            } else {
                holder.icon.isVisible = false
            }
            holder.text.text = model.text
        }
    }

    class Chip (
        @DrawableRes val icon: Int = 0,
        val text: String
    )

}