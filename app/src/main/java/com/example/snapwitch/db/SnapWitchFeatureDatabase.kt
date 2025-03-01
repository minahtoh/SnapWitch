package com.example.snapwitch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.snapwitch.ui.models.FeatureUsage

@Database(entities = [FeatureUsage::class], version = 1, exportSchema = false)
abstract class SnapWitchFeatureDatabase: RoomDatabase() {
    abstract fun getDao() : SnapWitchDao

    companion object{
        @Volatile
        private var INSTANCE : SnapWitchFeatureDatabase? = null

        fun getDatabase(context: Context): SnapWitchFeatureDatabase{
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SnapWitchFeatureDatabase::class.java,
                    "snapWitch_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance

                return instance
            }
        }
    }
}