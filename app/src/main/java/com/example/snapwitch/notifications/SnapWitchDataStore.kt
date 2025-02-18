package com.example.snapwitch.notifications

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private const val TAG = "SnapWitchDataStore"

// Creates DataStore instance
val Context.dataStore by preferencesDataStore(name = "notifications_store")


class SnapWitchDataStore(private val context: Context) {
  companion object{
      val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_list")
  }


    // Save notification
    suspend fun saveNotification(notification: NotificationData) {
        context.dataStore.edit {  preferences ->
            val existingNotificationsJson = preferences[NOTIFICATIONS_KEY].toString()

            val json = Json { ignoreUnknownKeys = true }
            val existingNotifications = try {
                json.decodeFromString<List<NotificationData>>(existingNotificationsJson)
            } catch (e: Exception) {
                emptyList()
            }.toMutableList()

            // Add the new notification
            existingNotifications.add(notification)

            preferences[NOTIFICATIONS_KEY] = Json.encodeToString(existingNotifications)
        }
    }

    val notificationsFlow: Flow<List<NotificationData>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[NOTIFICATIONS_KEY] as? String ?: "[]" // 🔹 Explicitly cast to String
            try {
                Json.decodeFromString<List<NotificationData>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }





    // Delete a notification
    suspend fun deleteNotification(notification: NotificationData) {
        context.dataStore.edit { preferences ->
        val existingNotificationsJson = preferences[NOTIFICATIONS_KEY] ?: "[]"

        val json = Json { ignoreUnknownKeys = true }
        val existingNotifications = try {
            json.decodeFromString<List<NotificationData>>(existingNotificationsJson)
        } catch (e: Exception) {
            emptyList()
        }.toMutableList()

        existingNotifications.removeAll { it.time == notification.time }

        // Save updated list back to DataStore
        preferences[NOTIFICATIONS_KEY] = json.encodeToString(existingNotifications)
    }
    }

    // Clear all notifications
    suspend fun clearNotifications() {
        context.dataStore.edit { prefs ->
            prefs.remove(NOTIFICATIONS_KEY)
        }
    }
}
