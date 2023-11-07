package com.herry.libs.widget.view.recyclerview.form.recycler

import android.content.Context
import android.view.View
import android.widget.TextView
import com.herry.libs.R
import com.herry.libs.nodeview.NodeForm
import com.herry.libs.nodeview.NodeHolder

class RecyclerViewEmptyTextForm : NodeForm<RecyclerViewEmptyTextForm.Holder, String>(Holder::class, String::class) {

    override fun onBindModel(context: Context, holder: Holder, model: String) {
        (holder.view as? TextView)?.text = model
    }

    override fun onLayout(): Int = R.layout.recyclerview_empty_text_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    inner class Holder(context: Context, view: View) : NodeHolder(context, view)

}