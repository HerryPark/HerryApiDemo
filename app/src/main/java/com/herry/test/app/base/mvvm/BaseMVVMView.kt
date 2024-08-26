package com.herry.test.app.base.mvvm

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.herry.libs.mvvm.MVVMView
import com.herry.test.app.base.BaseFragment

abstract class BaseMVVMView<VM: BaseMVVMViewModel>(private val viewmodelClass: Class<VM>): BaseFragment(), MVVMView<VM> {

    override val viewmodel: VM by lazy {
        ViewModelProvider(this)[viewmodelClass]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel.arguments.apply {
            this.clear()
            this.putAll(this@BaseMVVMView.arguments ?: Bundle())
        }

        //add LifeCycleObserver class to activity/fragment here
        lifecycle.addObserver(viewmodel)
    }
}