package com.example.snapwitch.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.snapwitch.R

class SnapWitchWifiNotification(private val context: Context) {
    companion object{
        const val CHANNEL_ID = "wifi_status"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(){
        val dataSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Manage Wi-Fi Connections")
            .setContentText("Time reached! turn off wifi network")
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    5,
                    dataSettingsIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setAutoCancel(true)
            .build()

        notificationManager.notify(7,notification)
    }
}