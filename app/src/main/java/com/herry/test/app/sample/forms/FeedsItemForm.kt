package com.herry.test.app.sample.forms

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bumptech.glide.Glide
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.sample.repository.database.feed.Feed
import java.util.*

class FeedsItemForm(
    private val onClickItem: ((form: FeedsItemForm, holder: Holder) -> Unit)?
): NodeForm<FeedsItemForm.Holder, Feed>(Holder::class, Feed::class) {
    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val container: View? = view.findViewById(R.id.feed_list_item_form_container)
        val cover: ImageView? = view.findViewById(R.id.feed_list_item_form_cover)

        init {
            container?.setOnSingleClickListener {
                onClickItem?.invoke(this@FeedsItemForm, this)
            }
        }
    }

    override fun onLayout(): Int = R.layout.feed_list_item_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Feed) {
        val constraintLayout = holder.view as? ConstraintLayout
        if (constraintLayout != null) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            holder.container?.let { container ->
                val width = model.width
                val height = model.height
                val dimensionRatio = String.format(Locale.ENGLISH, "H,%d:%d", width, height)
                constraintSet.setDimensionRatio(container.id, dimensionRatio)
                constraintSet.applyTo(constraintLayout)
            }
        }

        holder.cover?.let { cover ->
            Glide.with(context).load(model.imagePath)
                .placeholder(ColorDrawable(ViewUtil.getColor(context, R.color.tbc_70)))
                .into(cover)
        }
    }
}