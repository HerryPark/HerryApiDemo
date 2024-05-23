package com.herry.test.app.widgets.titleform

import android.os.Bundle
import android.view.View
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.databinding.TitleBarFragmentBinding
import com.herry.test.widget.TitleCompose
import com.herry.test.widget.TitleForm

class TitleFormFragment : BaseNavFragment(contentLayoutId = R.layout.title_bar_fragment) {
    private lateinit var binding: TitleBarFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context ?: return

        binding = TitleBarFragmentBinding.bind(view)

        view.findViewById<View?>(R.id.title_form_fragment_title_form)?.let {
            val titleForm = TitleForm()
            titleForm.bindHolder(context, it)
            titleForm.setTitle("Title Form/Compose")
            titleForm.setIconActionButton(to = TitleForm.ActionButton.START_FIRST, icon = R.drawable.ic_back)?.apply {
                setOnSingleClickListener { AppUtil.pressBackKey(activity) }
            }
        }

        binding.titleFormFragmentComposeView.setContent {
            TitleCompose()
        }
    }
}
