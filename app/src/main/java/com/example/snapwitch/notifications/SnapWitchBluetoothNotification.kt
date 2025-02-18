package com.example.snapwitch.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.snapwitch.R

class SnapWitchBluetoothNotification(private val context: Context) {
    companion object{
        const val CHANNEL_ID = "bluetooth_status"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(){
        val dataSettingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Manage Bluetooth Connections")
            .setContentText("Time reached! Tap to turn off bluetooth connections")
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    4,
                    dataSettingsIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(3,notification)
    }
}