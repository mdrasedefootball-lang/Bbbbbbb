package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey val id: String,
    val nameBn: String,
    val descriptionBn: String,
    val isEnabled: Boolean = true,
    val ruleCount: Int,
    val lastSync: String,
    val category: String
)

@Entity(tableName = "allowlist")
data class AllowlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val target: String,
    val type: String, // "WEBSITE" or "APP"
    val addedTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "app_protection")
data class AppProtectionEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isProtected: Boolean = true,
    val isSystemApp: Boolean = false
)

@Entity(tableName = "blocked_query_logs")
data class BlockedQueryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val category: String, // "ADS", "TRACKER", "THREAT"
    val timestamp: Long = System.currentTimeMillis(),
    val sourceApp: String = "Browser"
)

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val dateKey: String, // e.g. "2026-09-29"
    val dayOfWeekBn: String, // e.g. "সোম", "মঙ্গল"
    val adsBlocked: Int,
    val trackersBlocked: Int,
    val threatsBlocked: Int
)

enum class ProtectionState {
    ENABLED,
    DISABLED,
    PAUSED,
    STARTING,
    ERROR
}

enum class FilterSyncState {
    UP_TO_DATE,
    OUTDATED,
    UPDATING,
    FAILED,
    DISABLED
}
