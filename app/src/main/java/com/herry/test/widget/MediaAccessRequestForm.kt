package com.herry.test.widget

import android.content.Context
import android.view.View
import android.widget.TextView
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.NodeView
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnProtectClickListener
import com.herry.test.R

open class MediaAccessRequestForm(
    private val onGotoSettings: () -> Unit
) : NodeForm<MediaAccessRequestForm.Holder, MediaAccessRequestForm.Model>(Holder::class, Model::class) {

    data class Model(
        val message: String = ""
    )

    inner class Holder(context: Context, view: View): NodeHolder(context, view) {
        val messageView: TextView? = view.findViewById(R.id.media_access_request_from_message)
        private val gotoSettings: View? = view.findViewById(R.id.media_access_request_from_goto_settings)

        init {
            ViewUtil.setProtectTouchLowLayer(view, true)

            gotoSettings?.setOnProtectClickListener {
                onGotoSettings()
            }
        }
    }

    override fun onLayout(): Int = R.layout.media_access_request_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    override fun onBindModel(context: Context, holder: Holder, model: Model) {
        holder.messageView?.let { textView ->
            if (model.message.isNotEmpty()) {
                textView.text = model.message
            }
        }
    }
}