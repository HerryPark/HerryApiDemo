package com.herry.libs.util.network

interface OnNetworkConnectionChanged {
    fun onChangedNetwork(status: NetworkStatus)

    enum class NetworkStatus {
        CONNECTED,
        DISCONNECTED;

        fun isConnected(): Boolean = this == CONNECTED
        fun isDisconnected(): Boolean = this == DISCONNECTED
    }
}