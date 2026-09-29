package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FilterSyncState
import com.example.ui.MainViewModel
import com.example.ui.components.FilterCard
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary
import com.example.ui.theme.ShieldWarning
import com.example.ui.theme.ShieldWarningContainer

@Composable
fun FiltersScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null
) {
    val filters by viewModel.filterRules.collectAsState()
    val syncState by viewModel.filterSyncState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("filters_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShieldSurfaceElevated)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "পেছনে যান",
                                tint = ShieldTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ফিল্টার",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                        Text(
                            text = "কোন ধরনের কনটেন্ট ব্লক করা হবে তা নিয়ন্ত্রণ করুন।",
                            fontSize = 13.sp,
                            color = ShieldTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.syncFilters() },
                        enabled = syncState != FilterSyncState.UPDATING,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShieldSurfaceElevated)
                            .size(38.dp)
                    ) {
                        if (syncState == FilterSyncState.UPDATING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ShieldAccentGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "সিঙ্ক করুন",
                                tint = ShieldAccentGreen
                            )
                        }
                    }
                }
            }

            // Top Banner for Update / Sync
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncState == FilterSyncState.UPDATING) ShieldSurfaceCard else ShieldWarningContainer
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (syncState == FilterSyncState.UPDATING) ShieldAccentGreen else ShieldWarning
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (syncState == FilterSyncState.UPDATING) Icons.Default.Sync else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (syncState == FilterSyncState.UPDATING) ShieldAccentGreen else ShieldWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (syncState == FilterSyncState.UPDATING) "আপডেট হচ্ছে…" else "ফিল্টার আপডেট প্রস্তুত",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (syncState == FilterSyncState.UPDATING)
                                "নতুন ডোমেইন ও ট্র্যাকার নিয়ম সিঙ্ক করা হচ্ছে..."
                            else
                                "সর্বশেষ ফিল্টার রুলস ও ক্ষতিকর ডোমেইন ডেটাবেজ হালনাগাদ করতে এখনই আপডেট করুন।",
                            fontSize = 13.sp,
                            color = ShieldTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.syncFilters() },
                            enabled = syncState != FilterSyncState.UPDATING,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShieldAccentGreen,
                                contentColor = Color(0xFF042111)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("update_filters_now_button")
                        ) {
                            if (syncState == FilterSyncState.UPDATING) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF042111),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "আপডেট হচ্ছে…", fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Text(text = "এখনই আপডেট করুন", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // List of filter cards
            items(filters, key = { it.id }) { rule ->
                FilterCard(
                    title = rule.nameBn,
                    description = rule.descriptionBn,
                    ruleCount = rule.ruleCount,
                    lastSync = rule.lastSync,
                    isEnabled = rule.isEnabled,
                    onToggle = { isChecked ->
                        viewModel.toggleFilter(rule.id, isChecked)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
