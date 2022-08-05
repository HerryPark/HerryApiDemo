package com.herry.test.app.sample.home.form

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R

class HomeBottomNavControlItemForm(
    private val onClick: (id: Int) -> Unit
) : NodeForm<HomeBottomNavControlItemForm.Holder, HomeBottomNavControlItemForm.Model>(Holder::class, Model::class) {

    data class Model(
        val id: Int,
        @DrawableRes val icon: Int,
        val label: String?
    )

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val icon: ImageView = view.findViewById(R.id.home_bottom_nav_item_form_icon)
        val label: TextView = view.findViewById(R.id.home_bottom_nav_item_form_label)

        init {
            view.setOnClickListener {
                model?.let { model ->
                    onClick.invoke(model.id)
                }
            }
        }
    }

    override fun onLayout(): Int = R.layout.home_bottom_nav_item_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.icon.setImageResource(model.icon)
        holder.label.text = model.label
//        holder.label.isVisible = model.label != null
    }
}

