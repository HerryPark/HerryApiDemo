package com.herry.test.app.checker.password_setting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.herry.libs.app.nav.NavBundleUtil
import com.herry.libs.util.AppUtil
import com.herry.test.R
import com.herry.test.app.base.nav.BaseMVPNavView
import com.herry.test.widget.TitleBarForm

/**
 * Created by herry.park on 2020/7/7
 **/
class PasswordSettingFragment : BaseMVPNavView<PasswordSettingContract.View, PasswordSettingContract.Presenter>(), PasswordSettingContract.View {

    override fun onCreatePresenter(): PasswordSettingContract.Presenter =
        PasswordSettingPresenter()

    override fun onCreatePresenterView(): PasswordSettingContract.View = this

    private var container: View? = null

    private var inputPassword: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == this.container) {
            this.container = inflater.inflate(R.layout.password_setting_fragment, container, false)
            init(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        view ?: return

        TitleBarForm(
            activity = { requireActivity() },
            onClickBack = { AppUtil.pressBackKey(requireActivity(), view) }
        ).apply {
            bindFormHolder(view.context, view.findViewById(R.id.password_setting_fragment_title))
            bindFormModel(view.context, TitleBarForm.Model(title = "Input Password", backEnable = true))
        }

        inputPassword = view.findViewById(R.id.password_setting_fragment_password_editor)
        inputPassword?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.setPassword(s?.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    override fun onDisplayPassword(password: String) {
        inputPassword?.setText(password)
    }


    override fun getNavigateUpResult(): Bundle {
        return NavBundleUtil.createNavigationBundle(presenter?.isChangedPassword() ?: false)
    }
}