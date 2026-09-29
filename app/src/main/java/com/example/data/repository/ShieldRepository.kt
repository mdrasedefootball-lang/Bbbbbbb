package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.db.DFShieldDatabase
import com.example.data.model.AllowlistEntity
import com.example.data.model.AppProtectionEntity
import com.example.data.model.BlockedQueryLogEntity
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FilterRuleEntity
import com.example.data.model.FilterSyncState
import com.example.data.model.ProtectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShieldRepository(private val context: Context) {
    private val db = DFShieldDatabase.getDatabase(context)
    private val filterDao = db.filterDao()
    private val allowlistDao = db.allowlistDao()
    private val appProtectionDao = db.appProtectionDao()
    private val logsAndStatsDao = db.logsAndStatsDao()

    private val prefs = context.getSharedPreferences("df_shield_prefs", Context.MODE_PRIVATE)

    private val _protectionState = MutableStateFlow(
        try {
            ProtectionState.valueOf(prefs.getString("protection_state", ProtectionState.DISABLED.name) ?: ProtectionState.DISABLED.name)
        } catch (_: Exception) {
            ProtectionState.DISABLED
        }
    )
    val protectionState: StateFlow<ProtectionState> = _protectionState.asStateFlow()

    private val _filterSyncState = MutableStateFlow(FilterSyncState.UP_TO_DATE)
    val filterSyncState: StateFlow<FilterSyncState> = _filterSyncState.asStateFlow()

    private val _pauseRemainingSeconds = MutableStateFlow(0L)
    val pauseRemainingSeconds: StateFlow<Long> = _pauseRemainingSeconds.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Settings
    private val _httpsFiltering = MutableStateFlow(prefs.getBoolean("https_filtering", false))
    val httpsFiltering: StateFlow<Boolean> = _httpsFiltering.asStateFlow()

    private val _advancedRules = MutableStateFlow(prefs.getBoolean("advanced_rules", false))
    val advancedRules: StateFlow<Boolean> = _advancedRules.asStateFlow()

    private val _batterySaver = MutableStateFlow(prefs.getBoolean("battery_saver", false))
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _lowPowerMode = MutableStateFlow(prefs.getBoolean("low_power_mode", false))
    val lowPowerMode: StateFlow<Boolean> = _lowPowerMode.asStateFlow()

    private val _autoUpdate = MutableStateFlow(prefs.getBoolean("auto_update", true))
    val autoUpdate: StateFlow<Boolean> = _autoUpdate.asStateFlow()

    private val _updateWifiOnly = MutableStateFlow(prefs.getBoolean("update_wifi_only", true))
    val updateWifiOnly: StateFlow<Boolean> = _updateWifiOnly.asStateFlow()

    private val _privacyMode = MutableStateFlow(prefs.getBoolean("privacy_mode", true))
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    private val _diagnostics = MutableStateFlow(prefs.getBoolean("diagnostics", false))
    val diagnostics: StateFlow<Boolean> = _diagnostics.asStateFlow()

    private val _lastUpdateFormatted = MutableStateFlow(prefs.getString("last_update_str", "১২ মিনিট আগে") ?: "১২ মিনিট আগে")
    val lastUpdateFormatted: StateFlow<String> = _lastUpdateFormatted.asStateFlow()

    private val _youtubeAdsBlocked = MutableStateFlow(prefs.getInt("yt_ads_blocked", 142))
    val youtubeAdsBlocked: StateFlow<Int> = _youtubeAdsBlocked.asStateFlow()

    private val _facebookSponsoredBlocked = MutableStateFlow(prefs.getInt("fb_sponsored_blocked", 89))
    val facebookSponsoredBlocked: StateFlow<Int> = _facebookSponsoredBlocked.asStateFlow()

    fun incrementYouTubeBlocked() {
        val next = _youtubeAdsBlocked.value + 1
        _youtubeAdsBlocked.value = next
        prefs.edit().putInt("yt_ads_blocked", next).apply()
        repositoryScope.launch {
            recordBlockedQuery("googleads.g.doubleclick.net", "ADS", "YouTube Shield")
        }
    }

    fun incrementFacebookBlocked() {
        val next = _facebookSponsoredBlocked.value + 1
        _facebookSponsoredBlocked.value = next
        prefs.edit().putInt("fb_sponsored_blocked", next).apply()
        repositoryScope.launch {
            recordBlockedQuery("graph.facebook.com/sponsored", "ADS", "Facebook Shield")
        }
    }

    val filterRules: Flow<List<FilterRuleEntity>> = filterDao.getAllFilterRules()
    val allowlistEntries: Flow<List<AllowlistEntity>> = allowlistDao.getAllAllowlist()
    val appProtections: Flow<List<AppProtectionEntity>> = appProtectionDao.getAllAppProtections()
    val recentBlockedLogs: Flow<List<BlockedQueryLogEntity>> = logsAndStatsDao.getRecentBlockedLogs()
    val dailyStats: Flow<List<DailyStatsEntity>> = logsAndStatsDao.getDailyStats()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            initDefaultDataIfEmpty()
            checkPauseState()
        }
    }

    private suspend fun initDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        // Initialize default filters if not populated
        val defaultFilters = listOf(
            FilterRuleEntity(
                id = "ads",
                nameBn = "বিজ্ঞাপন ফিল্টার",
                descriptionBn = "বিজ্ঞাপন ও ব্যানার ব্লক করে",
                isEnabled = true,
                ruleCount = 45210,
                lastSync = "আজ, ১০:৪২",
                category = "ADS"
            ),
            FilterRuleEntity(
                id = "trackers",
                nameBn = "ট্র্যাকার ফিল্টার",
                descriptionBn = "ব্যবহারকারী ট্র্যাকিং ও ডেটা ট্র্যাকার ব্লক করে",
                isEnabled = true,
                ruleCount = 28450,
                lastSync = "আজ, ১০:৪২",
                category = "TRACKER"
            ),
            FilterRuleEntity(
                id = "malware",
                nameBn = "ম্যালওয়্যার সুরক্ষা",
                descriptionBn = "পরিচিত ক্ষতিকর ও সন্দেহজনক ডোমেইন প্রতিহত করে",
                isEnabled = true,
                ruleCount = 18920,
                lastSync = "আজ, ১০:৪২",
                category = "THREAT"
            ),
            FilterRuleEntity(
                id = "phishing",
                nameBn = "ফিশিং সুরক্ষা",
                descriptionBn = "ভুয়া ব্যাংকিং বা প্রতারণামূলক সাইট প্রতিহত করে",
                isEnabled = true,
                ruleCount = 12340,
                lastSync = "আজ, ১০:৪২",
                category = "THREAT"
            ),
            FilterRuleEntity(
                id = "popups",
                nameBn = "পপ-আপ সুরক্ষা",
                descriptionBn = "অনাকাঙ্ক্ষিত পপ-আপ ও রিডাইরেক্ট প্রতিরোধ করে",
                isEnabled = true,
                ruleCount = 8410,
                lastSync = "আজ, ১০:৪২",
                category = "ADS"
            ),
            FilterRuleEntity(
                id = "youtube_ads",
                nameBn = "YouTube বিজ্ঞাপন ও স্পনসর ফিল্টার",
                descriptionBn = "ভিডিও প্রি-রোল, মিড-রোল ও স্পনসর ব্যানার দমন করে",
                isEnabled = true,
                ruleCount = 52400,
                lastSync = "আজ, ১০:৪২",
                category = "ADS"
            ),
            FilterRuleEntity(
                id = "facebook_sponsor",
                nameBn = "Facebook স্পনসরড পোস্ট ফিল্টার",
                descriptionBn = "ফেসবুক ফিডের স্পনসরড পোস্ট, ভিডিও অ্যাড ও ট্র্যাকার ব্লক করে",
                isEnabled = true,
                ruleCount = 38200,
                lastSync = "আজ, ১০:৪২",
                category = "ADS"
            ),
            FilterRuleEntity(
                id = "annoyance",
                nameBn = "বিরক্তিকর কনটেন্ট",
                descriptionBn = "বিরক্তিকর নোটিফিকেশন প্রম্পট ও ভাসমান ব্যানার ব্লক",
                isEnabled = true,
                ruleCount = 6120,
                lastSync = "আজ, ১০:৪২",
                category = "ANNOYANCE"
            )
        )
        filterDao.insertFilterRules(defaultFilters)

        // Initialize default allowlist examples if empty
        if (allowlistDao.countTarget("example.com") == 0) {
            allowlistDao.insert(
                AllowlistEntity(
                    target = "example.com",
                    type = "ওয়েবসাইট",
                    notes = "টেস্টিং সাইট"
                )
            )
            allowlistDao.insert(
                AllowlistEntity(
                    target = "bkash.com",
                    type = "ওয়েবসাইট",
                    notes = "পেমেন্ট গেটওয়ে বিশ্বস্ত"
                )
            )
        }

        // Initialize installed/common apps
        if (appProtectionDao.getCount() == 0) {
            val installedList = mutableListOf<AppProtectionEntity>()
            try {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                for (app in packages) {
                    val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val name = pm.getApplicationLabel(app).toString()
                    val pkg = app.packageName
                    if (!isSystem || pkg.contains("chrome") || pkg.contains("youtube") || pkg.contains("browser")) {
                        installedList.add(
                            AppProtectionEntity(
                                packageName = pkg,
                                appName = name,
                                isProtected = true,
                                isSystemApp = isSystem
                            )
                        )
                    }
                    if (installedList.size >= 25) break
                }
            } catch (_: Exception) {}

            if (installedList.isEmpty()) {
                installedList.addAll(
                    listOf(
                        AppProtectionEntity("com.android.chrome", "Chrome", true, true),
                        AppProtectionEntity("org.mozilla.firefox", "Firefox", true, false),
                        AppProtectionEntity("com.google.android.youtube", "YouTube", false, true),
                        AppProtectionEntity("com.facebook.katana", "Facebook", true, false),
                        AppProtectionEntity("com.instagram.android", "Instagram", true, false),
                        AppProtectionEntity("com.whatsapp", "WhatsApp", true, false),
                        AppProtectionEntity("com.opera.browser", "Opera Browser", true, false),
                        AppProtectionEntity("com.microsoft.emmx", "Edge Browser", true, false)
                    )
                )
            }
            appProtectionDao.insertAll(installedList)
        }

        // Initialize stats matching prompt (12,482 ads, 3,841 trackers, 27 threats)
        val initialDailyStats = listOf(
            DailyStatsEntity("2026-09-24", "বুধ", 1850, 490, 3),
            DailyStatsEntity("2026-09-25", "বৃহস্পতি", 2410, 680, 5),
            DailyStatsEntity("2026-09-26", "শুক্র", 3120, 890, 8),
            DailyStatsEntity("2026-09-27", "শনি", 2180, 620, 4),
            DailyStatsEntity("2026-09-28", "রবি", 1676, 521, 3),
            DailyStatsEntity("2026-09-29", "সোম", 1246, 640, 4)
        )
        logsAndStatsDao.insertDailyStats(initialDailyStats)

        // Seed some sample blocked query logs for high fidelity feedback
        val sampleQueries = listOf(
            BlockedQueryLogEntity(domain = "googleads.g.doubleclick.net", category = "ADS", sourceApp = "Chrome"),
            BlockedQueryLogEntity(domain = "graph.facebook.com/telemetry", category = "TRACKER", sourceApp = "Instagram"),
            BlockedQueryLogEntity(domain = "app-measurement.com", category = "TRACKER", sourceApp = "Firefox"),
            BlockedQueryLogEntity(domain = "pagead2.googlesyndication.com", category = "ADS", sourceApp = "Chrome"),
            BlockedQueryLogEntity(domain = "track.adform.net", category = "TRACKER", sourceApp = "Opera"),
            BlockedQueryLogEntity(domain = "malicious-phish-alert.xyz", category = "THREAT", sourceApp = "Chrome")
        )
        for (q in sampleQueries) {
            logsAndStatsDao.insertLog(q)
        }
    }

    private suspend fun checkPauseState() {
        val pauseUntil = prefs.getLong("pause_until_timestamp", 0L)
        val now = System.currentTimeMillis()
        if (pauseUntil > now) {
            _protectionState.value = ProtectionState.PAUSED
            val remainingSec = (pauseUntil - now) / 1000
            _pauseRemainingSeconds.value = remainingSec
            startCountdown()
        } else if (_protectionState.value == ProtectionState.PAUSED) {
            setProtectionState(ProtectionState.ENABLED)
        }
    }

    private fun startCountdown() {
        repositoryScope.launch {
            while (_protectionState.value == ProtectionState.PAUSED) {
                val pauseUntil = prefs.getLong("pause_until_timestamp", 0L)
                val now = System.currentTimeMillis()
                val remainingSec = (pauseUntil - now) / 1000
                if (remainingSec <= 0) {
                    _pauseRemainingSeconds.value = 0
                    setProtectionState(ProtectionState.ENABLED)
                    break
                } else {
                    _pauseRemainingSeconds.value = remainingSec
                    delay(1000)
                }
            }
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _isOnboardingCompleted.value = completed
    }

    suspend fun setProtectionState(state: ProtectionState) = withContext(Dispatchers.IO) {
        prefs.edit().putString("protection_state", state.name).apply()
        if (state != ProtectionState.PAUSED) {
            prefs.edit().remove("pause_until_timestamp").apply()
            _pauseRemainingSeconds.value = 0
        }
        _protectionState.value = state
        if (state == ProtectionState.ENABLED) {
            _lastUpdateFormatted.value = "এখনই সক্রিয়"
            prefs.edit().putString("last_update_str", "এখনই সক্রিয়").apply()
        }
    }

    suspend fun pauseProtection(durationMillis: Long) = withContext(Dispatchers.IO) {
        val expiry = System.currentTimeMillis() + durationMillis
        prefs.edit()
            .putString("protection_state", ProtectionState.PAUSED.name)
            .putLong("pause_until_timestamp", expiry)
            .apply()
        _protectionState.value = ProtectionState.PAUSED
        _pauseRemainingSeconds.value = durationMillis / 1000
        startCountdown()
    }

    suspend fun resumeProtection() {
        setProtectionState(ProtectionState.ENABLED)
    }

    suspend fun toggleFilter(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        filterDao.setFilterEnabled(id, isEnabled)
    }

    suspend fun syncFilters(): Boolean = withContext(Dispatchers.IO) {
        _filterSyncState.value = FilterSyncState.UPDATING
        delay(1200) // realistic smooth sync simulation
        val currentTimeStr = SimpleDateFormat("আজ, hh:mm a", Locale.getDefault()).format(Date())
        filterDao.updateAllSyncTime(currentTimeStr)
        _filterSyncState.value = FilterSyncState.UP_TO_DATE
        _lastUpdateFormatted.value = "১ মিনিট আগে"
        prefs.edit().putString("last_update_str", "১ মিনিট আগে").apply()
        true
    }

    suspend fun addAllowlist(target: String, type: String, notes: String = "") = withContext(Dispatchers.IO) {
        allowlistDao.insert(
            AllowlistEntity(
                target = target.trim(),
                type = type,
                notes = notes
            )
        )
    }

    suspend fun removeAllowlist(id: Long) = withContext(Dispatchers.IO) {
        allowlistDao.deleteById(id)
    }

    suspend fun setAppProtection(packageName: String, isProtected: Boolean) = withContext(Dispatchers.IO) {
        appProtectionDao.setAppProtection(packageName, isProtected)
    }

    suspend fun setAllAppProtection(isProtected: Boolean) = withContext(Dispatchers.IO) {
        appProtectionDao.setAllAppProtection(isProtected)
    }

    suspend fun recordBlockedQuery(domain: String, category: String, sourceApp: String) = withContext(Dispatchers.IO) {
        logsAndStatsDao.insertLog(
            BlockedQueryLogEntity(
                domain = domain,
                category = category,
                sourceApp = sourceApp
            )
        )
    }

    // Setting updates
    fun updateHttpsFiltering(enabled: Boolean) {
        prefs.edit().putBoolean("https_filtering", enabled).apply()
        _httpsFiltering.value = enabled
    }

    fun updateAdvancedRules(enabled: Boolean) {
        prefs.edit().putBoolean("advanced_rules", enabled).apply()
        _advancedRules.value = enabled
    }

    fun updateBatterySaver(enabled: Boolean) {
        prefs.edit().putBoolean("battery_saver", enabled).apply()
        _batterySaver.value = enabled
    }

    fun updateLowPowerMode(enabled: Boolean) {
        prefs.edit().putBoolean("low_power_mode", enabled).apply()
        _lowPowerMode.value = enabled
    }

    fun updateAutoUpdate(enabled: Boolean) {
        prefs.edit().putBoolean("auto_update", enabled).apply()
        _autoUpdate.value = enabled
    }

    fun updateWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean("update_wifi_only", enabled).apply()
        _updateWifiOnly.value = enabled
    }

    fun updatePrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_mode", enabled).apply()
        _privacyMode.value = enabled
    }

    fun updateDiagnostics(enabled: Boolean) {
        prefs.edit().putBoolean("diagnostics", enabled).apply()
        _diagnostics.value = enabled
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        logsAndStatsDao.clearLogs()
        prefs.edit().clear().apply()
        _protectionState.value = ProtectionState.DISABLED
        _isOnboardingCompleted.value = false
        initDefaultDataIfEmpty()
    }
}
