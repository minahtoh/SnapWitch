package com.example.snapwitch.ui.utils

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.app.usage.NetworkStats
import android.content.SharedPreferences
import android.net.TrafficStats
import android.os.RemoteException
import java.util.*

class PhoneDataUsageManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("data_usage", Context.MODE_PRIVATE)

    data class DailyUsage(
        val mobileTotalBytes: Long,
        val wifiTotalBytes: Long
    )

    fun getTodayUsage(): DailyUsage {
        val lastUpdateTime = prefs.getLong("last_update_time", 0)
        val lastMobileTotal = prefs.getLong("last_mobile_total", 0)
        val lastWifiTotal = prefs.getLong("last_wifi_total", 0)

        val currentMobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        val currentTotal = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
        val currentWifi = currentTotal - currentMobile

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Reset counters at start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        if (lastUpdateTime < startOfDay) {
            // New day, reset counters
            prefs.edit()
                .putLong("last_update_time", now)
                .putLong("last_mobile_total", currentMobile)
                .putLong("last_wifi_total", currentWifi)
                .apply()
            return DailyUsage(0, 0)
        }

        val mobileUsage = if (currentMobile >= lastMobileTotal) currentMobile - lastMobileTotal else currentMobile
        val wifiUsage = if (currentWifi >= lastWifiTotal) currentWifi - lastWifiTotal else currentWifi

        return DailyUsage(mobileUsage, wifiUsage)
    }

    fun formatDataSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups])
    }
}