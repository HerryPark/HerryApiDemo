package com.herry.libs.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest


class NetworkConnectionChecker(context: Context, private val onConnection: OnConnection) {

    interface OnConnection {
        fun onConnected()
        fun onDisconnected()
    }

    private val connectivityManager: ConnectivityManager?
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onConnection.onConnected()
        }

        override fun onLost(network: Network) {
            onConnection.onDisconnected()
        }
    }

    init {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    fun register() {
        val connectivityManager = this.connectivityManager ?: return
        if (connectivityManager.activeNetwork == null) {
            onConnection.onDisconnected()
        }

        connectivityManager.registerNetworkCallback(networkRequest, connectivityCallback)
    }

    fun unregister() {
        val connectivityManager = this.connectivityManager ?: return
        connectivityManager.unregisterNetworkCallback(connectivityCallback)
    }

    fun isConnected(): Boolean = connectivityManager?.activeNetwork != null
}