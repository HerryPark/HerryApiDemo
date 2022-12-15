package com.herry.test.app.checker.password_setting

import com.herry.libs.data_checker.DataCheckerChangeData
import com.herry.libs.util.preferences.PreferenceHelper
import com.herry.test.sharedpref.SharedPrefKeys

/**
 * Created by herry.park on 2020/7/7
 **/
class PasswordSettingPresenter : PasswordSettingContract.Presenter() {

    private val passwordChecker: DataCheckerChangeData<String> = DataCheckerChangeData()

    override fun onResume(view: PasswordSettingContract.View, state: ResumeState) {
        if (state.isLaunch()) {
            if (state == ResumeState.LAUNCH) {
                passwordChecker.setBase(PreferenceHelper.get(SharedPrefKeys.PASSWORD, ""))
            }
            // sets list items
            display()
        }
    }

    private fun display() {
        view?.getViewContext() ?: return

        view?.onDisplayPassword(passwordChecker.getData() ?: "")
    }

    override fun setPassword(password: String?) {
        passwordChecker.setData(password ?: "")
    }

    override fun isChangedPassword(): Boolean {
        val result = passwordChecker.isChanged()
        if (result) {
            PreferenceHelper.set(SharedPrefKeys.PASSWORD, passwordChecker.getData())
        }
        return result
    }
}