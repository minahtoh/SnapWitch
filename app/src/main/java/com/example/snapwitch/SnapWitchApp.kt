package com.example.snapwitch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.example.snapwitch.notifications.SnapWitchBluetoothNotification
import com.example.snapwitch.notifications.SnapWitchNetworkNotification
import com.example.snapwitch.notifications.SnapWitchWifiNotification

class SnapWitchApp:Application() {
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            val dataChannel = NotificationChannel(
                SnapWitchNetworkNotification.CHANNEL_ID,
                "Network Scheduler",
                NotificationManager.IMPORTANCE_HIGH
            )
            dataChannel.description = "Set status for Network connection"

            val bluetoothChannel = NotificationChannel(
                SnapWitchBluetoothNotification.CHANNEL_ID,
                "Bluetooth Scheduler",
                NotificationManager.IMPORTANCE_HIGH
            )
            bluetoothChannel.description = "Set status for bluetooth connection"

            val wifiChannel = NotificationChannel(
                SnapWitchWifiNotification.CHANNEL_ID,
                "Wifi Scheduler",
                NotificationManager.IMPORTANCE_HIGH
            )
            wifiChannel.description = "Set status for wifi connections"

            val notificationService = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationService.apply {
                createNotificationChannel(dataChannel)
                createNotificationChannel(bluetoothChannel)
                createNotificationChannel(wifiChannel)
            }
        }
    }
}