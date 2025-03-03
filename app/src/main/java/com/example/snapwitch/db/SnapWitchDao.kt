package com.example.snapwitch.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.snapwitch.ui.models.FeatureUsage
import com.example.snapwitch.ui.models.FeatureUsageDaily

@Dao
interface SnapWitchDao {
    @Insert
    suspend fun insertUsage(usage: FeatureUsage)

    @Query("SELECT * FROM feature_usage WHERE settingName = :feature ORDER BY timestamp DESC")
    suspend fun getUsageForFeature(feature: String): List<FeatureUsage>

    @Query("""
    SELECT 
        strftime('%m-%d', datetime(timestamp / 1000, 'unixepoch')) AS day,
        settingName,
        COUNT(*) AS count 
    FROM feature_usage 
    WHERE settingName = :feature
    GROUP BY day 
    ORDER BY day ASC
            """)
    suspend fun getDailyFeatureUsage(feature: String): List<FeatureUsageDaily>

    @Query("SELECT COUNT(*) FROM feature_usage WHERE settingName = :feature")
    suspend fun getFeatureUsageCount(feature: String): Int
}