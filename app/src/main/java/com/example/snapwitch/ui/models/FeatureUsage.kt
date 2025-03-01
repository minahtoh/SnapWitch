package com.example.snapwitch.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_usage")
data class FeatureUsage(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val settingName: String,
    val timeStamp : Long
)
