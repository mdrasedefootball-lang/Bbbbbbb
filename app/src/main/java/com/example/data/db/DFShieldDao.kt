package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AllowlistEntity
import com.example.data.model.AppProtectionEntity
import com.example.data.model.BlockedQueryLogEntity
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Query("SELECT * FROM filter_rules ORDER BY id ASC")
    fun getAllFilterRules(): Flow<List<FilterRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterRules(rules: List<FilterRuleEntity>)

    @Update
    suspend fun updateFilterRule(rule: FilterRuleEntity)

    @Query("UPDATE filter_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setFilterEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE filter_rules SET lastSync = :syncTime")
    suspend fun updateAllSyncTime(syncTime: String)
}

@Dao
interface AllowlistDao {
    @Query("SELECT * FROM allowlist ORDER BY addedTimestamp DESC")
    fun getAllAllowlist(): Flow<List<AllowlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AllowlistEntity): Long

    @Query("DELETE FROM allowlist WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM allowlist WHERE LOWER(target) = LOWER(:target)")
    suspend fun countTarget(target: String): Int
}

@Dao
interface AppProtectionDao {
    @Query("SELECT * FROM app_protection ORDER BY appName ASC")
    fun getAllAppProtections(): Flow<List<AppProtectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppProtectionEntity>)

    @Query("UPDATE app_protection SET isProtected = :isProtected WHERE packageName = :packageName")
    suspend fun setAppProtection(packageName: String, isProtected: Boolean)

    @Query("UPDATE app_protection SET isProtected = :isProtected")
    suspend fun setAllAppProtection(isProtected: Boolean)

    @Query("SELECT COUNT(*) FROM app_protection")
    suspend fun getCount(): Int
}

@Dao
interface LogsAndStatsDao {
    @Query("SELECT * FROM blocked_query_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentBlockedLogs(): Flow<List<BlockedQueryLogEntity>>

    @Insert
    suspend fun insertLog(log: BlockedQueryLogEntity)

    @Query("SELECT * FROM daily_stats ORDER BY dateKey ASC")
    fun getDailyStats(): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStats(stats: List<DailyStatsEntity>)

    @Query("SELECT SUM(adsBlocked) as ads, SUM(trackersBlocked) as trackers, SUM(threatsBlocked) as threats FROM daily_stats")
    suspend fun getTotalStats(): StatsTotals?

    @Query("DELETE FROM blocked_query_logs")
    suspend fun clearLogs()
}

data class StatsTotals(
    val ads: Int?,
    val trackers: Int?,
    val threats: Int?
)
