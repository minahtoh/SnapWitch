package com.example.snapwitch.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val title: String,
    val message: String,
    val time : Long,
    val icon : String = "",
    val repeatDays : List<String> = emptyList()
)
