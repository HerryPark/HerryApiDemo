package com.herry.test.app.sample.feeds.search

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.herry.libs.nodeview.NodeHolder
import com.herry.libs.nodeview.NodeView
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.libs.widget.view.editor.EditTextEx
import com.herry.test.R

class SearchInputEditForm(
    private val onTextChanged: ((text: String) -> Unit)? = null,
    private val onFocusChange: ((text: String, hasFocus: Boolean) -> Unit)? = null,
    private val onEditorActionListener: TextView.OnEditorActionListener? = null
) : NodeView<SearchInputEditForm.Holder>() {

    override fun onLayout(): Int = R.layout.search_input_form

    override fun onCreateHolder(context: Context, view: View): Holder = Holder(context, view)

    inner class Holder(context: Context, view: View) : NodeHolder(context, view) {
        val input: EditTextEx? = view.findViewById(R.id.search_input_form_edit)
        val deleteView: View? = view.findViewById(R.id.search_input_form_clear_text)

        init {
            input?.let { input ->
                input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(100))
                input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        onTextChanged?.run { this(input.text.toString()) }

                        deleteView?.isVisible = s.isNotEmpty()
                    }
                })
                input.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    onFocusChange?.run { this(input.text.toString(), hasFocus) }
                }

                onEditorActionListener?.let {
                    input.setOnEditorActionListener(it)
                }
            }

            deleteView?.setOnSingleClickListener {
                input?.let { input ->
                    if (!input.text.isNullOrEmpty()) {
                        input.setText("")
                        if (!input.hasFocus()) {
                            input.requestFocusAndShowInput()
                        }
                    }
                }
            }
        }
    }

    fun setInputHint(hint: String, textColor: Int, hideHintOnFocus: Boolean = true) {
        holder?.input?.setBaseHint(hint)
        holder?.input?.setHintTextColor(textColor)
        holder?.input?.setHideHintOnFocus(hideHintOnFocus)
    }

    fun setText(text: String) {
        holder?.input?.setText(text)
    }

    fun getText() = holder?.input?.text

    fun requestFocus(focus: Boolean, withKeyboard: Boolean = false) {
        val view: EditTextEx = holder?.input ?: return

        if (withKeyboard) {
            if (focus) view.requestFocusAndShowInput() else view.clearFocusAndHideInput()
        } else {
            ViewUtil.requestFocus(holder?.input, focus)
        }
    }

    fun hasFocus() : Boolean = holder?.input?.hasFocus() ?: false
}