package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.SettingsItem
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldDanger
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldWarning

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val httpsFiltering by viewModel.httpsFiltering.collectAsState()
    val advancedRules by viewModel.advancedRules.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val lowPowerMode by viewModel.lowPowerMode.collectAsState()
    val autoUpdate by viewModel.autoUpdate.collectAsState()
    val updateWifiOnly by viewModel.updateWifiOnly.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = ShieldSurfaceCard,
            title = {
                Text(text = "DF Shield সম্পর্কে", color = ShieldTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "DF Shield v1.0.0", color = ShieldAccentGreen, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "প্রিমিয়াম প্রাইভেসি ও অনাকাঙ্ক্ষিত অ্যাড-ব্লকিং সমাধান। লোকাল ফার্স্ট ও ব্যাটারি-সাশ্রয়ী ফিল্টারিং ইঞ্জিন।",
                        color = ShieldTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldAccentGreen)
                ) {
                    Text("ঠিক আছে", color = ShieldBackground)
                }
            }
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            containerColor = ShieldSurfaceCard,
            title = {
                Text(text = "ওপেন সোর্স লাইসেন্স", color = ShieldTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "DF Shield utilizes Android Jetpack Compose, Kotlin Coroutines, Room Database, and Material 3 under the Apache 2.0 License.",
                    color = ShieldTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLicensesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldAccentGreen)
                ) {
                    Text("ঠিক আছে", color = ShieldBackground)
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = ShieldSurfaceCard,
            title = {
                Text(text = "সকল ডেটা রিসেট করবেন?", color = ShieldTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "এটি আপনার সমস্ত লোকাল পরিসংখ্যান, সংরক্ষিত কাস্টম রুলস এবং Allowlist মুছে প্রাথমিক অবস্থায় ফিরিয়ে আনবে।",
                    color = ShieldTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldDanger)
                ) {
                    Text("রিসেট করুন", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("বাতিল", color = ShieldTextPrimary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Text(
                        text = "সেটিংস",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShieldTextPrimary
                    )
                    Text(
                        text = "DF Shield কনফিগারেশন ও প্রিফারেন্স",
                        fontSize = 13.sp,
                        color = ShieldTextSecondary
                    )
                }
            }

            // Section: সাধারণ
            item {
                SettingsSectionCard(title = "সাধারণ") {
                    SettingsItem(
                        title = "ডার্ক মোড",
                        description = "অ্যামোলেড ডার্ক থিম সক্রিয়",
                        icon = Icons.Default.DarkMode,
                        checked = true,
                        onCheckedChange = {}
                    )
                    SettingsItem(
                        title = "ভাষা",
                        description = "বাংলা (ডিফল্ট)",
                        icon = Icons.Default.Language,
                        onClick = { viewModel.showFeedback("ভাষা: বাংলা সক্রিয়") }
                    )
                    SettingsItem(
                        title = "নোটিফিকেশন",
                        description = "সুরক্ষা স্থিতি ও আপডেট নোটিফিকেশন",
                        icon = Icons.Default.Notifications,
                        checked = true,
                        onCheckedChange = { viewModel.showFeedback("নোটিফিকেশন প্রিফারেন্স সংরক্ষিত") }
                    )
                }
            }

            // Section: সুরক্ষা
            item {
                SettingsSectionCard(title = "সুরক্ষা") {
                    SettingsItem(
                        title = "HTTPS ফিল্টারিং",
                        description = "ওয়েবসাইটের নিরাপদ ট্র্যাফিকের এনক্রিপ্টেড পপ-আপ প্রতিরোধ (রুট প্রয়োজন হতে পারে)",
                        icon = Icons.Default.Security,
                        checked = httpsFiltering,
                        onCheckedChange = { viewModel.updateHttpsFiltering(it) }
                    )
                    SettingsItem(
                        title = "উন্নত নিয়ম",
                        description = "কাস্টম রেজেক্স ও ইউজার স্ক্রিপ্ট ফিল্টারিং",
                        checked = advancedRules,
                        onCheckedChange = { viewModel.updateAdvancedRules(it) }
                    )
                    SettingsItem(
                        title = "হুমকি সুরক্ষা",
                        description = "পরিচিত ফিশিং ও ক্ষতিকর সাইট সক্রিয় ব্লক",
                        checked = true,
                        onCheckedChange = { viewModel.showFeedback("হুমকি সুরক্ষা সিস্টেম সক্রিয়") }
                    )
                }
            }

            // Section: ফিল্টার
            item {
                SettingsSectionCard(title = "ফিল্টার") {
                    SettingsItem(
                        title = "স্বয়ংক্রিয় আপডেট",
                        description = "নিয়মিত নতুন ক্ষতিকর ডোমেইন রুলস আপডেট",
                        icon = Icons.Default.Sync,
                        checked = autoUpdate,
                        onCheckedChange = { viewModel.updateAutoUpdate(it) }
                    )
                    SettingsItem(
                        title = "Wi-Fi-তে আপডেট",
                        description = "ওয়াইফাই কানেকশনে থাকলে ডেটাবেজ সিঙ্ক হবে",
                        checked = updateWifiOnly,
                        onCheckedChange = { viewModel.updateWifiOnly(it) }
                    )
                }
            }

            // Section: পারফরম্যান্স
            item {
                SettingsSectionCard(title = "পারফরম্যান্স") {
                    SettingsItem(
                        title = "ব্যাটারি-সাশ্রয়ী মোড",
                        description = "স্ক্রিন অফ থাকলে ফিল্টার স্লিপ মোড চালু হবে",
                        icon = Icons.Default.BatteryChargingFull,
                        checked = batterySaver,
                        onCheckedChange = { viewModel.updateBatterySaver(it) }
                    )
                    SettingsItem(
                        title = "কম পাওয়ার মোড",
                        description = "লগিং কমিয়ে মেমোরি সংরক্ষণ করে",
                        checked = lowPowerMode,
                        onCheckedChange = { viewModel.updateLowPowerMode(it) }
                    )
                }
            }

            // Section: গোপনীয়তা
            item {
                SettingsSectionCard(title = "গোপনীয়তা") {
                    SettingsItem(
                        title = "Privacy Mode",
                        description = "সমস্ত ডেটা ডিভাইসের সুরক্ষিত মেমরিতে থাকে",
                        icon = Icons.Default.Lock,
                        checked = privacyMode,
                        onCheckedChange = { viewModel.updatePrivacyMode(it) }
                    )
                    SettingsItem(
                        title = "ডায়াগনস্টিক ডেটা",
                        description = "ত্রুটি বিশ্লেষণ রিপোর্ট (ঐচ্ছিক)",
                        checked = diagnostics,
                        onCheckedChange = { viewModel.updateDiagnostics(it) }
                    )
                }
            }

            // Section: অন্যান্য
            item {
                SettingsSectionCard(title = "অন্যান্য") {
                    SettingsItem(
                        title = "FAQ / সাহায্য",
                        description = "সাধারণ প্রশ্ন ও সমাধান দেখুন",
                        icon = Icons.Default.HelpOutline,
                        onClick = { onNavigate(Screen.FaqHelp) }
                    )
                    SettingsItem(
                        title = "Privacy Policy",
                        description = "আমাদের ডেটা সুরক্ষার নিয়মাবলী",
                        icon = Icons.Default.Description,
                        onClick = { onNavigate(Screen.PrivacyPolicy) }
                    )
                    SettingsItem(
                        title = "ওপেন সোর্স লাইসেন্স",
                        description = "ব্যবহৃত লাইব্রেরি ও ক্রেডিট",
                        onClick = { showLicensesDialog = true }
                    )
                    SettingsItem(
                        title = "অ্যাপ সম্পর্কে",
                        description = "DF Shield সংস্করণ ও পরিচিতি",
                        icon = Icons.Default.Info,
                        onClick = { showAboutDialog = true }
                    )
                    SettingsItem(
                        title = "ডেটা রিসেট",
                        description = "সকল সেটিংস ও ইতিহাস মুছে ফেলুন",
                        icon = Icons.Default.RestartAlt,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShieldAccentGreen,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}
