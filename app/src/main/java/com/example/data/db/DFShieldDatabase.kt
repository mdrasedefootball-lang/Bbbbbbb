package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AllowlistEntity
import com.example.data.model.AppProtectionEntity
import com.example.data.model.BlockedQueryLogEntity
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FilterRuleEntity

@Database(
    entities = [
        FilterRuleEntity::class,
        AllowlistEntity::class,
        AppProtectionEntity::class,
        BlockedQueryLogEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DFShieldDatabase : RoomDatabase() {
    abstract fun filterDao(): FilterDao
    abstract fun allowlistDao(): AllowlistDao
    abstract fun appProtectionDao(): AppProtectionDao
    abstract fun logsAndStatsDao(): LogsAndStatsDao

    companion object {
        @Volatile
        private var INSTANCE: DFShieldDatabase? = null

        fun getDatabase(context: Context): DFShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DFShieldDatabase::class.java,
                    "df_shield_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
