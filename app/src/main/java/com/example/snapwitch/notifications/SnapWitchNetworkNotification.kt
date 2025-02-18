package com.example.snapwitch.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.core.app.NotificationCompat
import com.example.snapwitch.R
import com.example.snapwitch.viewmodel.SnapWitchViewModel

class SnapWitchNetworkNotification(private val context: Context) {

    companion object{
        const val CHANNEL_ID = "network_status"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(){
        val dataSettingsIntent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Manage Network Connections")
            .setContentText("Time reached! Tap to turn off data connection")
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    5,
                    dataSettingsIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(6,notification)

    }

}