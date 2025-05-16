package com.herry.test.app.base.mvvm

import android.os.Bundle
import android.view.View
import com.herry.test.app.base.BaseFragment

abstract class BaseMVVMFragment<VM: BaseViewModel>: BaseFragment() {

    protected lateinit var viewModel: VM
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel = onViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        viewLifecycleOwner.lifecycle.removeObserver(viewModel)

        super.onDestroyView()
    }

    abstract fun onViewModel(): VM
}