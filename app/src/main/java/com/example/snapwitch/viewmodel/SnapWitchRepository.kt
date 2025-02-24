package com.example.snapwitch.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

class SnapWitchRepository(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val repoContext = context

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()



    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    data class ConnectivityStatus(
        val isWifiEnabled: Boolean = false,
        val isBluetoothEnabled: Boolean = false
    )


    fun observeConnectivityStatus(context: Context = repoContext): Flow<ConnectivityStatus> = callbackFlow {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val isWifiEnabled = wifiManager.isWifiEnabled

                trySend(
                    ConnectivityStatus(
                        isWifiEnabled = isWifiEnabled,
                        isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
                    )
                )
            }
        }

        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false

                trySend(
                    ConnectivityStatus(
                        isWifiEnabled = wifiManager.isWifiEnabled,
                        isBluetoothEnabled = isBluetoothEnabled
                    )
                )
            }

        }

        repoContext.registerReceiver(
            wifiReceiver,
            IntentFilter().apply {
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            }
        )

        repoContext.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )

        awaitClose {
            repoContext.unregisterReceiver(wifiReceiver)
            repoContext.unregisterReceiver(bluetoothReceiver)
        }

    }
}