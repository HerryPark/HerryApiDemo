package com.herry.test.app.widgets.appbutton

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.view.AppButton
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.TitleBarForm

class AppButtonsFragment: BaseNavFragment() {
    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.app_buttons_fragment, container, false)?.also {
            this.container = it
            init(it)
        }
    }

    private fun init(view: View){
        val context = view.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.app_buttons_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Styleable Buttons", backEnable = true))
        }

        view.findViewById<ViewGroup?>(R.id.app_buttons_fragment_programmatically_container)?.let { container ->
            AppButton(context, R.style.AppButton_Icon).apply {
                this.setIcon(R.drawable.ic_settings)
                this.setOnClickListener {}
            }.also { appButton ->
                container.addView(appButton)
            }

            AppButton(context, R.style.AppButton_Standard_Outline).apply {
                this.setIcon(R.drawable.ic_settings)
                this.setText("Test")
                this.setOnClickListener {}
            }.also { appButton ->
                container.addView(appButton)
            }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_1)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_icon_normal)?.let { button ->
            button.setOnClickListener {}
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_icon_selected)?.let { button ->
            button.isSelected = true
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_icon_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_sold)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_sold_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_accent)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_accent_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_gradient_accent)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_standard_fill_gradient_accent_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_text_only)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_text_only_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_small)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_small_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_small_fill_accent)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_small_fill_accent_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_large)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_large_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_large_fill_solid)?.let { button ->
            button.setOnClickListener { }
        }

        view.findViewById<View?>(R.id.app_buttons_fragment_button_large_fill_solid_disabled)?.let { button ->
            ViewUtil.setViewGroupEnabled(button, false)
        }
    }
}