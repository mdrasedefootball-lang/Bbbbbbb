package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldAccentGreenContainer
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary
import com.example.ui.theme.ShieldWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel
) {
    var selectedPeriodIndex by remember { mutableIntStateOf(1) } // 0: দিন, 1: সপ্তাহ, 2: মাস
    val periods = listOf("দিন", "সপ্তাহ", "মাস")

    val dailyStats by viewModel.dailyStats.collectAsState()
    val recentLogs by viewModel.recentBlockedLogs.collectAsState()

    val adsTotal = when (selectedPeriodIndex) {
        0 -> "1,246"
        1 -> "12,482"
        else -> "48,930"
    }
    val trackersTotal = when (selectedPeriodIndex) {
        0 -> "640"
        1 -> "3,841"
        else -> "14,510"
    }
    val threatsTotal = when (selectedPeriodIndex) {
        0 -> "4"
        1 -> "27"
        else -> "112"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("statistics_screen")
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
                        text = "পরিসংখ্যান",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShieldTextPrimary
                    )
                    Text(
                        text = "অন-ডিভাইস ফিল্টারিং ও ব্লকিং রেকর্ড",
                        fontSize = 13.sp,
                        color = ShieldTextSecondary
                    )
                }
            }

            // Period Selector
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    periods.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                            onClick = { selectedPeriodIndex = index },
                            selected = index == selectedPeriodIndex,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = ShieldAccentGreen,
                                activeContentColor = Color(0xFF042111),
                                inactiveContainerColor = ShieldSurfaceElevated,
                                inactiveContentColor = ShieldTextSecondary,
                                activeBorderColor = ShieldBorder,
                                inactiveBorderColor = ShieldBorder
                            )
                        ) {
                            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Metric Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "বিজ্ঞাপন",
                        count = adsTotal,
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.Shield,
                        iconColor = ShieldAccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ট্র্যাকার",
                        count = trackersTotal,
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.TrackChanges,
                        iconColor = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "হুমকি",
                        count = threatsTotal,
                        subtitle = "ব্লক করা হয়েছে",
                        icon = Icons.Default.Warning,
                        iconColor = ShieldWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Activity Chart Card
            item {
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ব্লক করা আইটেম",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            Text(
                                text = "সাপ্তাহিক ট্রেন্ড",
                                fontSize = 12.sp,
                                color = ShieldTextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom High Polish Canvas Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val paddingBottom = 24.dp.toPx()
                                val chartH = h - paddingBottom

                                // Draw horizontal grid lines
                                val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1f)
                                gridLevels.forEach { lvl ->
                                    val y = chartH * (1f - lvl)
                                    drawLine(
                                        color = Color(0xFF222E3C),
                                        start = Offset(0f, y),
                                        end = Offset(w, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Data points for days
                                val dataPoints = listOf(
                                    Pair("বুধ", 0.45f),
                                    Pair("বৃহঃ", 0.65f),
                                    Pair("শুক্র", 0.90f),
                                    Pair("শনি", 0.60f),
                                    Pair("রবি", 0.40f),
                                    Pair("সোম", 0.35f)
                                )

                                val stepX = w / (dataPoints.size)
                                val barWidth = 24.dp.toPx()

                                dataPoints.forEachIndexed { i, point ->
                                    val xCenter = stepX * i + stepX / 2
                                    val barH = chartH * point.second
                                    val barTop = chartH - barH

                                    // Draw bar with subtle gradient / rounded top
                                    drawRoundRect(
                                        color = if (i == 2) ShieldAccentGreen else Color(0xFF1E523A),
                                        topLeft = Offset(xCenter - barWidth / 2, barTop),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Day Labels under chart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val days = listOf("বুধ", "বৃহস্পতি", "শুক্র", "শনি", "রবি", "সোম")
                            days.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = ShieldTextTertiary
                                )
                            }
                        }
                    }
                }
            }

            // Section 11 Card: আজকের সারাংশ
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ShieldAccentGreen.copy(alpha = 0.3f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ShieldAccentGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ShieldAccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "আজকের সারাংশ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "আজ DF Shield মোট ১,২৪৬টি অনাকাঙ্ক্ষিত সংযোগ ব্লক করেছে। আপনার গোপনীয়তা অক্ষুণ্ণ রয়েছে।",
                                fontSize = 13.sp,
                                color = ShieldTextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // Live Activity Query Log Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক ব্লকিং কার্যক্রম",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShieldTextPrimary
                    )
                    Text(
                        text = "ডিভাইসে সংরক্ষিত",
                        fontSize = 12.sp,
                        color = ShieldTextTertiary
                    )
                }
            }

            // Log items
            items(recentLogs, key = { it.id }) { log ->
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (log.category) {
                                            "ADS" -> ShieldAccentGreenContainer
                                            "TRACKER" -> Color(0xFF152A3E)
                                            else -> Color(0xFF3E151A)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (log.category) {
                                        "ADS" -> Icons.Default.Block
                                        "TRACKER" -> Icons.Default.TrackChanges
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    tint = when (log.category) {
                                        "ADS" -> ShieldAccentGreen
                                        "TRACKER" -> Color(0xFF64B5F6)
                                        else -> ShieldWarning
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = log.domain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ShieldTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${log.sourceApp} • ${log.category}",
                                    fontSize = 11.sp,
                                    color = ShieldTextTertiary
                                )
                            }
                        }

                        Text(
                            text = timeStr,
                            fontSize = 11.sp,
                            color = ShieldTextTertiary
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
