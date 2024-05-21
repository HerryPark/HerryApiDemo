package com.herry.test.app.font.downloadable

import android.content.Context
import android.view.View
import android.widget.TextView
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R

class FontListItemForm(
    private val onClickItem: (form: FontListItemForm, holder: Holder) -> Unit
): NodeForm<FontListItemForm.Holder, FontListItemForm.Model>(Holder::class, Model::class) {
    data class Model(
        val name: String,
        var isSelected: Boolean
    )

    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val name: TextView? = view.findViewById(R.id.font_list_item_form_label)

        init {
            view.setOnSingleClickListener {
                onClickItem(this@FontListItemForm, this@Holder)
            }
        }
    }

    override fun onLayout(): Int = R.layout.font_list_item_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.name?.let { name ->
            name.text = model.name
        }

        holder.view.isSelected = model.isSelected
    }
}