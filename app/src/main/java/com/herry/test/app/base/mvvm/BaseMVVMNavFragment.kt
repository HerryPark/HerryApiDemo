package com.herry.test.app.base.mvvm

import android.os.Bundle
import com.herry.test.app.base.nav.BaseNavFragment

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseMVVMNavFragment<VM: BaseViewModel>: BaseNavFragment() {

    protected lateinit var viewModel: VM
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(onViewModel().also { this.viewModel = it })

        onUIState()
    }

    abstract fun onViewModel(): VM

    protected open fun onUIState() {}
}