package com.example.snapwitch.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.edit
import com.example.snapwitch.notifications.NotificationData
import com.example.snapwitch.notifications.SnapWitchBluetoothNotification
import com.example.snapwitch.notifications.SnapWitchDataStore
import com.example.snapwitch.notifications.SnapWitchNetworkNotification
import com.example.snapwitch.notifications.SnapWitchWifiNotification
import com.example.snapwitch.notifications.dataStore
import com.example.snapwitch.ui.presentation.logToFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.log


class SnapWitchReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getStringExtra("ACTION_TYPE")
        when (action) {
            "TOGGLE_WIFI" -> context?.let {
                toggleWiFi(it)
            }
            "TOGGLE_BLUETOOTH" -> context?.let {
                toggleBluetooth(it)
            }
            "TOGGLE_DATA" -> context?.let {
                toggleData(it)

            }
            "TOGGLE_WIFI_NOTIFICATION" -> context?.let {
                toggleWiFiFromNotification(it)
                saveNotification(
                    context,
                    title = "Wifi Scheduler",
                    message = "Scheduler time due! adjust status now",
                    icon = "success"
                )
                logToFirebase(schedulerType = "wifi", "notificationFired")
            }
            "TOGGLE_BLUETOOTH_NOTIFICATION" -> context?.let {
                toggleBluetoothFromNotification(it)
                saveNotification(
                    context,
                    title = "Bluetooth Scheduler",
                    message = "Scheduler time due! adjust status now",
                    icon = "success"
                )
                logToFirebase(schedulerType = "bluetooth", "notificationFired")
            }
            "TOGGLE_DATA_NOTIFICATION" -> context?.let {
                toggleDataFromNotification(it)
                saveNotification(
                    context,
                    title = "Network Scheduler",
                    message = "Scheduler time due! adjust status now",
                    icon = "success"
                )
                logToFirebase(schedulerType = "data", "notificationFired")
            }
            }
        }
    }

    private fun saveNotification(
        context: Context,
        title: String,
        message: String,
        icon : String
    )
    {
        val notification = NotificationData(
            title = title,
            message = message,
            time = System.currentTimeMillis(),
            icon = icon
        )
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("SnapWitchReceiver", "saveNotification: notificationSaved")
            context.dataStore.edit{preferences ->
                val existingNotificationsJson = preferences[SnapWitchDataStore.NOTIFICATIONS_KEY] ?: "[]"

                val json = Json { ignoreUnknownKeys = true }
                val existingNotifications = try {
                    json.decodeFromString<List<NotificationData>>(existingNotificationsJson )
                } catch (e: Exception) {
                    emptyList()
                }.toMutableList()

                // Add the new notification
                existingNotifications.add(notification)

                preferences[SnapWitchDataStore.NOTIFICATIONS_KEY] =
                    json.encodeToString(existingNotifications) // ✅ Store as a single String

            }
        }
    }

    private fun toggleBluetooth(context: Context) {
        val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(settingsIntent)
    }

    private fun toggleWiFi(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled){
                wifiManager.setWifiEnabled(true)
            }
        }

        val dataSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dataSettingsIntent)

    }

    private fun toggleData(context: Context){
        val dataSettingsIntent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dataSettingsIntent)
    }

    private fun toggleDataFromNotification(context: Context){
        val service = SnapWitchNetworkNotification(context.applicationContext)
        service.showNotification()
    }

    private fun toggleBluetoothFromNotification(context: Context){
        val service = SnapWitchBluetoothNotification(context.applicationContext)
        service.showNotification()
    }

    private fun toggleWiFiFromNotification(context: Context){
        val service = SnapWitchWifiNotification(context.applicationContext)
        service.showNotification()
    }

