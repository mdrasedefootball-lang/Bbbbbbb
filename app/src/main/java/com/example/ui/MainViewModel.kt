package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AllowlistEntity
import com.example.data.model.AppProtectionEntity
import com.example.data.model.BlockedQueryLogEntity
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FilterRuleEntity
import com.example.data.model.FilterSyncState
import com.example.data.model.ProtectionState
import com.example.data.repository.ShieldRepository
import com.example.vpn.DFShieldVpnService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object OnboardingWelcome : Screen()
    data object OnboardingSetup : Screen()
    data object Home : Screen()
    data object Statistics : Screen()
    data object Filters : Screen()
    data object Settings : Screen()
    data object Allowlist : Screen()
    data object AppManagement : Screen()
    data object YouTubeSupport : Screen()
    data object FacebookShield : Screen()
    data object FaqHelp : Screen()
    data object PrivacyPolicy : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShieldRepository(application)

    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Home).apply {
        viewModelScope.launch {
            repository.isOnboardingCompleted.collect { completed ->
                if (!completed) {
                    _screenStack.value = listOf(Screen.OnboardingWelcome)
                } else if (_screenStack.value.firstOrNull() is Screen.OnboardingWelcome || _screenStack.value.firstOrNull() is Screen.OnboardingSetup) {
                    _screenStack.value = listOf(Screen.Home)
                }
            }
        }
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Home
            }
        }
    }

    val protectionState: StateFlow<ProtectionState> = repository.protectionState
    val filterSyncState: StateFlow<FilterSyncState> = repository.filterSyncState
    val pauseRemainingSeconds: StateFlow<Long> = repository.pauseRemainingSeconds
    val lastUpdateFormatted: StateFlow<String> = repository.lastUpdateFormatted

    val httpsFiltering: StateFlow<Boolean> = repository.httpsFiltering
    val advancedRules: StateFlow<Boolean> = repository.advancedRules
    val batterySaver: StateFlow<Boolean> = repository.batterySaver
    val lowPowerMode: StateFlow<Boolean> = repository.lowPowerMode
    val autoUpdate: StateFlow<Boolean> = repository.autoUpdate
    val updateWifiOnly: StateFlow<Boolean> = repository.updateWifiOnly
    val privacyMode: StateFlow<Boolean> = repository.privacyMode
    val diagnostics: StateFlow<Boolean> = repository.diagnostics

    val filterRules: StateFlow<List<FilterRuleEntity>> = repository.filterRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allowlistEntries: StateFlow<List<AllowlistEntity>> = repository.allowlistEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appProtections: StateFlow<List<AppProtectionEntity>> = repository.appProtections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBlockedLogs: StateFlow<List<BlockedQueryLogEntity>> = repository.recentBlockedLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyStats: StateFlow<List<DailyStatsEntity>> = repository.dailyStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val youtubeAdsBlocked: StateFlow<Int> = repository.youtubeAdsBlocked
    val facebookSponsoredBlocked: StateFlow<Int> = repository.facebookSponsoredBlocked

    fun recordYouTubeBlocked() = repository.incrementYouTubeBlocked()
    fun recordFacebookBlocked() = repository.incrementFacebookBlocked()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _vpnPermissionIntent = MutableSharedFlow<Intent>()
    val vpnPermissionIntent: SharedFlow<Intent> = _vpnPermissionIntent.asSharedFlow()

    private val _pendingYouTubeUrl = MutableStateFlow<String?>(null)
    val pendingYouTubeUrl: StateFlow<String?> = _pendingYouTubeUrl.asStateFlow()

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        // Avoid duplicate pushes
        if (current.lastOrNull() != screen) {
            current.add(screen)
            _screenStack.value = current
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            return true
        }
        return false
    }

    fun switchBottomTab(screen: Screen) {
        // Base tabs are Home, Statistics, Filters, Settings
        _screenStack.value = listOf(screen)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            _screenStack.value = listOf(Screen.Home)
        }
    }

    fun startProtectionWithVpnCheck(context: Context) {
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            viewModelScope.launch {
                _vpnPermissionIntent.emit(prepareIntent)
            }
        } else {
            activateVpnProtection(context)
        }
    }

    fun onVpnPermissionGranted(context: Context) {
        activateVpnProtection(context)
    }

    private fun activateVpnProtection(context: Context) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, DFShieldVpnService::class.java).apply {
                    action = DFShieldVpnService.ACTION_START
                }
                context.startService(intent)
                repository.setProtectionState(ProtectionState.ENABLED)
                showFeedback("সুরক্ষা চালু হয়েছে।")
            } catch (e: Exception) {
                e.printStackTrace()
                repository.setProtectionState(ProtectionState.ERROR)
                showFeedback("কাজটি সম্পন্ন করা যায়নি। আবার চেষ্টা করুন।")
            }
        }
    }

    fun disableProtection(context: Context) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, DFShieldVpnService::class.java).apply {
                    action = DFShieldVpnService.ACTION_STOP
                }
                context.startService(intent)
                repository.setProtectionState(ProtectionState.DISABLED)
                showFeedback("সুরক্ষা বন্ধ করা হয়েছে।")
            } catch (e: Exception) {
                e.printStackTrace()
                showFeedback("কাজটি সম্পন্ন করা যায়নি। আবার চেষ্টা করুন।")
            }
        }
    }

    fun pauseProtection(context: Context, durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, DFShieldVpnService::class.java).apply {
                    action = DFShieldVpnService.ACTION_STOP
                }
                context.startService(intent)
                repository.pauseProtection(durationMinutes * 60 * 1000L)
                showFeedback("সুরক্ষা সাময়িকভাবে বিরতিতে আছে।")
            } catch (e: Exception) {
                e.printStackTrace()
                showFeedback("কাজটি সম্পন্ন করা যায়নি। আবার চেষ্টা করুন।")
            }
        }
    }

    fun resumeProtection(context: Context) {
        startProtectionWithVpnCheck(context)
    }

    fun syncFilters() {
        viewModelScope.launch {
            val success = repository.syncFilters()
            if (success) {
                showFeedback("ফিল্টার সফলভাবে আপডেট হয়েছে।")
            } else {
                showFeedback("কাজটি সম্পন্ন করা যায়নি। আবার চেষ্টা করুন।")
            }
        }
    }

    fun toggleFilter(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleFilter(id, isEnabled)
        }
    }

    fun addAllowlistEntry(target: String, type: String, notes: String = "") {
        if (target.isBlank()) return
        viewModelScope.launch {
            repository.addAllowlist(target, type, notes)
            showFeedback("সাইটটি Allowlist-এ যোগ হয়েছে।")
        }
    }

    fun removeAllowlistEntry(id: Long) {
        viewModelScope.launch {
            repository.removeAllowlist(id)
            showFeedback("Allowlist থেকে সরানো হয়েছে।")
        }
    }

    fun toggleAppProtection(packageName: String, isProtected: Boolean) {
        viewModelScope.launch {
            repository.setAppProtection(packageName, isProtected)
        }
    }

    fun setAllAppProtection(isProtected: Boolean) {
        viewModelScope.launch {
            repository.setAllAppProtection(isProtected)
            if (isProtected) {
                showFeedback("সব অ্যাপের জন্য সুরক্ষা চালু করা হয়েছে।")
            } else {
                showFeedback("সব অ্যাপের জন্য সুরক্ষা বন্ধ করা হয়েছে।")
            }
        }
    }

    fun handleSharedYouTubeUrl(text: String?) {
        if (text == null) return
        val url = extractUrl(text)
        if (url != null) {
            _pendingYouTubeUrl.value = url
            navigateTo(Screen.YouTubeSupport)
        }
    }

    fun setYouTubeUrl(url: String) {
        _pendingYouTubeUrl.value = url
    }

    private fun extractUrl(text: String): String? {
        val parts = text.split("\\s+".toRegex())
        for (part in parts) {
            if (part.startsWith("http://") || part.startsWith("https://")) {
                return part
            }
        }
        return if (text.contains("youtu")) text else null
    }

    fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Setting handlers
    fun updateHttpsFiltering(v: Boolean) = repository.updateHttpsFiltering(v)
    fun updateAdvancedRules(v: Boolean) = repository.updateAdvancedRules(v)
    fun updateBatterySaver(v: Boolean) = repository.updateBatterySaver(v)
    fun updateLowPowerMode(v: Boolean) = repository.updateLowPowerMode(v)
    fun updateAutoUpdate(v: Boolean) = repository.updateAutoUpdate(v)
    fun updateWifiOnly(v: Boolean) = repository.updateWifiOnly(v)
    fun updatePrivacyMode(v: Boolean) = repository.updatePrivacyMode(v)
    fun updateDiagnostics(v: Boolean) = repository.updateDiagnostics(v)
    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            showFeedback("সকল সেটিংস ও হিস্ট্রি রিসেট করা হয়েছে।")
        }
    }
}
