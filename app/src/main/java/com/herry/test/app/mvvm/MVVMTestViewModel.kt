package com.herry.test.app.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.herry.test.app.base.mvvm.BaseMVVMViewModel

class MVVMTestViewModel: BaseMVVMViewModel() {
    private val _counts: MutableLiveData<Int?> = MutableLiveData(null)
    val counts: LiveData<Int?> = _counts

    fun increase() {
        val count: Int = counts.value ?: 0

        _counts.value = count + 1
    }

    fun decrease() {
        val count: Int = counts.value ?: 0

        _counts.value = count - 1
    }
}