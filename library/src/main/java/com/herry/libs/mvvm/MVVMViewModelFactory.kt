package com.herry.libs.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MVVMViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val clazz = modelClass.getDeclaredConstructor().newInstance()
        if (clazz is MVVMViewModel) {
//            clazz.arguments.clear()
//            if (context is Fragment) {
//                clazz.arguments.putAll(context.arguments ?: Bundle())
//            }
        }
        return super.create(modelClass)
    }
}