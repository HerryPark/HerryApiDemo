package com.herry.test.app.base.mvvm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herry.libs.util.network.NetworkConnectionChecker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel: ViewModel() {

    private val _onNetworkConnection = MutableSharedFlow<Boolean>()
    protected val onNetworkConnection: SharedFlow<Boolean> = _onNetworkConnection.asSharedFlow()

    private var networkConnectionChecker: NetworkConnectionChecker? = null

    private var mvvmCheckerType: MVVMChecker.Type = MVVMChecker.Type.NONE

    override fun onCleared() {
        super.onCleared()

        networkConnectionChecker = null
    }

    internal fun setMVVMChecker(context: Context?, type: MVVMChecker.Type) {
        context ?: return

        mvvmCheckerType = type
        when (type) {
            MVVMChecker.Type.NETWORK -> {
                // registry network connection checker to the view model
                setNetworkConnectionChecker(NetworkConnectionChecker(context))
            }
            MVVMChecker.Type.NONE -> {}
        }
    }

    internal fun startMVVMChecker() {
        when (mvvmCheckerType) {
            MVVMChecker.Type.NONE -> {}
            MVVMChecker.Type.NETWORK -> {
                startNetworkConnectionChecker()
            }
        }
    }

    internal fun stopMVVMChecker() {
        when (mvvmCheckerType) {
            MVVMChecker.Type.NONE -> {}
            MVVMChecker.Type.NETWORK -> {
                stopNetworkConnectionChecker()
            }
        }
    }

    private fun setNetworkConnectionChecker(checker: NetworkConnectionChecker) {
        this.networkConnectionChecker = checker
        this.networkConnectionChecker?.setOnConnection(object : NetworkConnectionChecker.OnConnection {
            override fun onConnected() {
                setChangedNetworkConnection(true)
            }

            override fun onDisconnected() {
                setChangedNetworkConnection(false)
            }
        })
    }

    private fun startNetworkConnectionChecker() {
        networkConnectionChecker?.register()
    }

    private fun stopNetworkConnectionChecker() {
        networkConnectionChecker?.unregister()
    }

    private fun setChangedNetworkConnection(on: Boolean) {
        viewModelScope.launch {
            _onNetworkConnection.emit(on)
        }
    }

    protected fun isConnectedNetwork(): Boolean = networkConnectionChecker?.isConnected() == true

    protected fun isConnectedWiFi(): Boolean = networkConnectionChecker?.getNetworkType() == NetworkConnectionChecker.NetworkType.WIFI
}