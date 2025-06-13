package com.herry.libs.util.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission


class NetworkConnectionChecker(context: Context, listener: OnConnection? = null) {

    interface OnConnection {
        fun onConnected()
        fun onDisconnected()
    }

    private var onConnection: OnConnection? = listener

    fun setOnConnection(listener: OnConnection) {
        this.onConnection = listener
    }

    private val availableSet = mutableSetOf<Network>()
    private var connectivityManager: ConnectivityManager? = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        // adds the network capability (NetworkCapabilities.NET_CAPABILITY_INTERNET) for the internet network connectivity checking or not
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val isPrevDisconnected = availableSet.isEmpty()
            availableSet.add(network)
            if (isPrevDisconnected) {
                onConnection?.onConnected()
            }
        }

        override fun onLost(network: Network) {
            val isPrevConnected = availableSet.isNotEmpty()
            availableSet.remove(network)
            if (isPrevConnected && availableSet.isEmpty()) {
                onConnection?.onDisconnected()
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun register() {
        val connectivityManager = this.connectivityManager ?: return
        availableSet.clear()
        if (connectivityManager.activeNetwork == null) {
            onConnection?.onDisconnected()
        }

        connectivityManager.registerNetworkCallback(networkRequest, connectivityCallback)
    }

    fun unregister() {
        val connectivityManager = this.connectivityManager ?: return
        try {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(): Boolean = connectivityManager?.activeNetwork != null

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkType(): NetworkType {
        return getNetworkType(connectivityManager?.activeNetwork)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun getNetworkType(network: Network?): NetworkType {
        return try {
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                else -> NetworkType.UNKNOWN
            }
        } catch (_: Exception) {
            NetworkType.UNKNOWN
        }
    }

    enum class NetworkType{
        WIFI,
        CELLULAR,
        UNKNOWN
    }
}