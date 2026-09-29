package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtectionState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.PauseBottomSheet
import com.example.ui.components.ProtectionStatusCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBanner
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldAccentGreenContainer
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldDanger
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary
import com.example.ui.theme.ShieldWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val protectionState by viewModel.protectionState.collectAsState()
    val lastUpdate by viewModel.lastUpdateFormatted.collectAsState()
    val pauseSeconds by viewModel.pauseRemainingSeconds.collectAsState()
    val feedbackMsg by viewModel.feedbackMessage.collectAsState()
    val httpsFiltering by viewModel.httpsFiltering.collectAsState()
    val ytBlocked by viewModel.youtubeAdsBlocked.collectAsState()
    val fbBlocked by viewModel.facebookSponsoredBlocked.collectAsState()

    var showPauseSheet by remember { mutableStateOf(false) }
    val pauseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showPauseSheet) {
        PauseBottomSheet(
            onDismiss = { showPauseSheet = false },
            onSelectDuration = { minutes ->
                viewModel.pauseProtection(context, minutes)
            },
            sheetState = pauseSheetState
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Top Bar / Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DF Shield",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (protectionState) {
                                            ProtectionState.ENABLED -> ShieldAccentGreen
                                            ProtectionState.PAUSED -> ShieldWarning
                                            else -> ShieldDanger
                                        }
                                    )
                            )
                        }
                        Text(
                            text = "আপনার সুরক্ষা",
                            fontSize = 14.sp,
                            color = ShieldTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { onNavigate(Screen.YouTubeSupport) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ShieldSurfaceElevated)
                                .size(40.dp)
                                .testTag("home_youtube_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "YouTube সুরক্ষা",
                                tint = ShieldAccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onNavigate(Screen.Settings) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ShieldSurfaceElevated)
                                .size(40.dp)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "সেটিংস",
                                tint = ShieldTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Feedback banner if active
            if (feedbackMsg != null) {
                item {
                    StatusBanner(
                        message = feedbackMsg!!,
                        onDismiss = { viewModel.clearFeedback() }
                    )
                }
            }

            // Main Protection Card
            item {
                ProtectionStatusCard(
                    state = protectionState,
                    lastUpdate = lastUpdate,
                    pauseRemainingSeconds = pauseSeconds,
                    onToggle = { enable ->
                        if (enable) {
                            viewModel.startProtectionWithVpnCheck(context)
                        } else {
                            viewModel.disableProtection(context)
                        }
                    },
                    onPauseClick = { showPauseSheet = true }
                )
            }

            // Section: Protection Statistics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "বিজ্ঞাপন",
                        count = "12,482",
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.Shield,
                        iconColor = ShieldAccentGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "home_ads_stat"
                    )
                    StatCard(
                        title = "ট্র্যাকার",
                        count = "3,841",
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.TrackChanges,
                        iconColor = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f),
                        testTag = "home_tracker_stat"
                    )
                    StatCard(
                        title = "হুমকি",
                        count = "27",
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.Warning,
                        iconColor = ShieldWarning,
                        modifier = Modifier.weight(1f),
                        testTag = "home_threats_stat"
                    )
                }
            }

            // Section: Smart Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_summary_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "স্মার্ট সারাংশ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            TextButton(
                                onClick = { onNavigate(Screen.Filters) },
                                modifier = Modifier.testTag("summary_details_button")
                            ) {
                                Text(
                                    text = "বিস্তারিত দেখুন",
                                    fontSize = 13.sp,
                                    color = ShieldAccentGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        SummaryCheckItem(text = "বিজ্ঞাপন ফিল্টার সক্রিয়", isChecked = true)
                        SummaryCheckItem(text = "ট্র্যাকার সুরক্ষা সক্রিয়", isChecked = true)
                        SummaryCheckItem(text = "হুমকি সুরক্ষা সক্রিয়", isChecked = true)
                        SummaryCheckItem(
                            text = if (httpsFiltering) "HTTPS ফিল্টারিং চালু" else "HTTPS ফিল্টারিং বন্ধ",
                            isChecked = httpsFiltering,
                            isDim = !httpsFiltering
                        )
                    }
                }
            }

            // Dedicated Social & Video Ad Blockers
            item {
                Text(
                    text = "সোশ্যাল ও ভিডিও অ্যাড ব্লকার",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShieldTextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // YouTube Ad Shield Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(Screen.YouTubeSupport) }
                            .testTag("home_youtube_shield_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ShieldDanger.copy(alpha = 0.4f))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ShieldDanger.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = ShieldDanger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ShieldAccentGreenContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${ytBlocked}টি ব্লকড",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShieldAccentGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "YouTube অ্যাড ব্লকার",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ভিডিও প্রি-রোল ও ব্যানার মুক্ত",
                                fontSize = 11.sp,
                                color = ShieldTextSecondary
                            )
                        }
                    }

                    // Facebook Sponsor Shield Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(Screen.FacebookShield) }
                            .testTag("home_facebook_shield_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1877F2).copy(alpha = 0.4f))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1877F2).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF1877F2),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ShieldAccentGreenContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${fbBlocked}টি ব্লকড",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShieldAccentGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Facebook স্পনসর",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ফিড স্পনসরড পোস্ট দমন",
                                fontSize = 11.sp,
                                color = ShieldTextSecondary
                            )
                        }
                    }
                }
            }

            // Section: Quick Actions Header
            item {
                Text(
                    text = "দ্রুত কার্যক্রম",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShieldTextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Four Compact Quick Action Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            icon = Icons.Default.Pause,
                            iconTint = ShieldWarning,
                            title = "⏸ বিরতি",
                            subtitle = if (protectionState == ProtectionState.PAUSED) "আবার চালু করুন" else "সাময়িক বন্ধ",
                            onClick = {
                                if (protectionState == ProtectionState.PAUSED) {
                                    viewModel.resumeProtection(context)
                                } else {
                                    showPauseSheet = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            icon = Icons.Default.FactCheck,
                            iconTint = ShieldAccentGreen,
                            title = "✓ Allowlist",
                            subtitle = "অনুমোদিত সাইট",
                            onClick = { onNavigate(Screen.Allowlist) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            icon = Icons.Default.FilterList,
                            iconTint = Color(0xFF64B5F6),
                            title = "🛡 ফিল্টার",
                            subtitle = "তালিকা পরিচালনা",
                            onClick = { onNavigate(Screen.Filters) },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            icon = Icons.Default.BarChart,
                            iconTint = Color(0xFFBA68C8),
                            title = "📊 পরিসংখ্যান",
                            subtitle = "ব্লকিং কার্যক্রম",
                            onClick = { onNavigate(Screen.Statistics) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick App Management Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(Screen.AppManagement) }
                        .testTag("home_app_management_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShieldSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ShieldAccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "অ্যাপ সুরক্ষা পরিচালনা",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShieldTextPrimary
                                )
                                Text(
                                    text = "কোন অ্যাপ ফিল্টার হবে তা ঠিক করুন",
                                    fontSize = 12.sp,
                                    color = ShieldTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ShieldTextTertiary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCheckItem(
    text: String,
    isChecked: Boolean,
    isDim: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Pause,
            contentDescription = null,
            tint = if (isChecked) ShieldAccentGreen else ShieldTextTertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isDim) ShieldTextTertiary else ShieldTextPrimary
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShieldTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ShieldTextSecondary
            )
        }
    }
}
