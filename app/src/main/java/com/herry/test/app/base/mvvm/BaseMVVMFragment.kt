package com.herry.test.app.base.mvvm

import android.os.Bundle
import com.herry.test.app.base.BaseFragment

abstract class BaseMVVMFragment<VM: BaseViewModel>: BaseFragment() {

    protected lateinit var viewModel: VM
        private set

    protected open val mvvmChecker: MVVMChecker = MVVMChecker(type = MVVMChecker.Type.NONE, enforce = MVVMChecker.Enforce.ON_START)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = onViewModel()
        viewModel.setMVVMChecker(context, mvvmChecker.type)
    }

    override fun onStart() {
        super.onStart()

        if (mvvmChecker.enforce == MVVMChecker.Enforce.ON_START) {
            viewModel.startMVVMChecker()
        }
    }

    override fun onResume() {
        super.onResume()

        if (mvvmChecker.enforce == MVVMChecker.Enforce.ON_RESUME) {
            viewModel.startMVVMChecker()
        }
    }

    override fun onPause() {

        if (mvvmChecker.enforce == MVVMChecker.Enforce.ON_RESUME) {
            viewModel.stopMVVMChecker()
        }

        super.onPause()
    }

    override fun onStop() {
        if (mvvmChecker.enforce == MVVMChecker.Enforce.ON_START) {
            viewModel.stopMVVMChecker()
        }

        super.onStop()
    }

    private fun startMVVMChecker() {

    }

    private fun stopMVVMChecker() {

    }

    abstract fun onViewModel(): VM

}