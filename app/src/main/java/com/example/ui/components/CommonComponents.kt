package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtectionState
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldAccentGreenContainer
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldBorderLight
import com.example.ui.theme.ShieldDanger
import com.example.ui.theme.ShieldDangerContainer
import com.example.ui.theme.ShieldSurface
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary
import com.example.ui.theme.ShieldWarning
import com.example.ui.theme.ShieldWarningContainer

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = ShieldAccentGreen,
    contentColor: Color = Color(0xFF042111),
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = ShieldSurfaceElevated,
            disabledContentColor = ShieldTextTertiary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ShieldTextPrimary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusBanner(
    message: String,
    type: StatusBannerType = StatusBannerType.SUCCESS,
    onDismiss: () -> Unit = {}
) {
    val (bgColor, borderColor, icon, iconColor) = when (type) {
        StatusBannerType.SUCCESS -> Quad(ShieldAccentGreenContainer, ShieldAccentGreen, Icons.Default.Check, ShieldAccentGreen)
        StatusBannerType.WARNING -> Quad(ShieldWarningContainer, ShieldWarning, Icons.Default.Warning, ShieldWarning)
        StatusBannerType.ERROR -> Quad(ShieldDangerContainer, ShieldDanger, Icons.Default.Close, ShieldDanger)
        StatusBannerType.INFO -> Quad(ShieldSurfaceElevated, ShieldBorder, Icons.Default.Info, ShieldTextSecondary)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("status_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = ShieldTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "বন্ধ করুন",
                    tint = ShieldTextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

enum class StatusBannerType {
    SUCCESS, WARNING, ERROR, INFO
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ProtectionStatusCard(
    state: ProtectionState,
    lastUpdate: String,
    pauseRemainingSeconds: Long,
    onToggle: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = state == ProtectionState.ENABLED
    val isPaused = state == ProtectionState.PAUSED
    val isOff = state == ProtectionState.DISABLED || state == ProtectionState.ERROR

    val cardBorderColor = when {
        isEnabled -> ShieldAccentGreen.copy(alpha = 0.4f)
        isPaused -> ShieldWarning.copy(alpha = 0.4f)
        else -> ShieldDanger.copy(alpha = 0.3f)
    }

    val glowColor = when {
        isEnabled -> ShieldAccentGreenContainer.copy(alpha = 0.45f)
        isPaused -> ShieldWarningContainer.copy(alpha = 0.45f)
        else -> ShieldDangerContainer.copy(alpha = 0.35f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_protection_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(cardBorderColor)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glowColor)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shield Icon with status halo
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isEnabled -> ShieldAccentGreen.copy(alpha = 0.18f)
                                    isPaused -> ShieldWarning.copy(alpha = 0.18f)
                                    else -> ShieldDanger.copy(alpha = 0.18f)
                                }
                            )
                            .border(
                                1.5.dp,
                                when {
                                    isEnabled -> ShieldAccentGreen
                                    isPaused -> ShieldWarning
                                    else -> ShieldDanger
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isEnabled -> Icons.Default.Shield
                                isPaused -> Icons.Default.Pause
                                else -> Icons.Default.Warning
                            },
                            contentDescription = "সুরক্ষা স্থিতি",
                            tint = when {
                                isEnabled -> ShieldAccentGreen
                                isPaused -> ShieldWarning
                                else -> ShieldDanger
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main switch / toggle
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF042111),
                            checkedTrackColor = ShieldAccentGreen,
                            uncheckedThumbColor = ShieldTextTertiary,
                            uncheckedTrackColor = ShieldSurfaceElevated,
                            uncheckedBorderColor = ShieldBorder
                        ),
                        modifier = Modifier.testTag("protection_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Description
                Text(
                    text = when {
                        isEnabled -> "সুরক্ষা চালু"
                        isPaused -> "সুরক্ষা বিরতিতে আছে"
                        else -> "সুরক্ষা বন্ধ"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShieldTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isEnabled -> "ইন্টারনেট সম্পূর্ণ গতিতে সচল আছে এবং শুধু অনাকাঙ্ক্ষিত বিজ্ঞাপন ও ট্র্যাকার ব্লক হচ্ছে।"
                        isPaused -> {
                            val min = (pauseRemainingSeconds / 60)
                            "আবার চালু হবে: ${if (min > 0) "$min মিনিট পরে" else "$pauseRemainingSeconds সেকেন্ড পরে"}"
                        }
                        else -> "আপনার সুরক্ষা বন্ধ আছে। শুধু বিজ্ঞাপন ব্লক করতে চালু করুন।"
                    },
                    fontSize = 14.sp,
                    color = ShieldTextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action or Sub-info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সর্বশেষ আপডেট: $lastUpdate",
                        fontSize = 12.sp,
                        color = ShieldTextTertiary
                    )

                    if (isOff) {
                        PrimaryButton(
                            text = "সুরক্ষা চালু করুন",
                            onClick = { onToggle(true) },
                            containerColor = ShieldAccentGreen,
                            contentColor = Color(0xFF042111),
                            modifier = Modifier.width(160.dp),
                            testTag = "enable_protection_cta"
                        )
                    } else if (isEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShieldSurfaceElevated)
                                .clickable { onPauseClick() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = ShieldWarning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "বিরতি দিন",
                                    fontSize = 12.sp,
                                    color = ShieldTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (isPaused) {
                        PrimaryButton(
                            text = "আবার চালু করুন",
                            onClick = { onToggle(true) },
                            containerColor = ShieldWarning,
                            contentColor = Color(0xFF291800),
                            modifier = Modifier.width(150.dp),
                            testTag = "resume_protection_cta"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    subtitle: String = "ব্লক করা হয়েছে",
    icon: ImageVector,
    iconColor: Color = ShieldAccentGreen,
    modifier: Modifier = Modifier,
    testTag: String = "stat_card"
) {
    Card(
        modifier = modifier
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = ShieldTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ShieldTextTertiary
            )
        }
    }
}

@Composable
fun FilterCard(
    title: String,
    description: String,
    ruleCount: Int,
    lastSync: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("filter_card_${title}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isEnabled) ShieldAccentGreen.copy(alpha = 0.25f) else ShieldBorder
            )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = ShieldTextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF042111),
                        checkedTrackColor = ShieldAccentGreen,
                        uncheckedThumbColor = ShieldTextTertiary,
                        uncheckedTrackColor = ShieldSurfaceElevated,
                        uncheckedBorderColor = ShieldBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ShieldBorder.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) ShieldAccentGreen else ShieldTextTertiary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnabled) "সক্রিয় ($ruleCount নিয়ম)" else "বন্ধ",
                        fontSize = 12.sp,
                        color = if (isEnabled) ShieldAccentGreen else ShieldTextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "শেষ সিঙ্ক: $lastSync",
                    fontSize = 11.sp,
                    color = ShieldTextTertiary
                )
            }
        }
    }
}

@Composable
fun AllowlistItem(
    target: String,
    type: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("allowlist_item_${target}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ShieldTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ShieldSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            color = ShieldTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "এই সাইটে সুরক্ষা বন্ধ",
                        fontSize = 11.sp,
                        color = ShieldWarning
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "বিকল্প",
                        tint = ShieldTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(ShieldSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("সরান", color = ShieldDanger) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    appName: String,
    packageName: String,
    isProtected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_item_${packageName}"),
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShieldSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isProtected) ShieldAccentGreen else ShieldTextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ShieldTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = packageName,
                    fontSize = 12.sp,
                    color = ShieldTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = isProtected,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF042111),
                    checkedTrackColor = ShieldAccentGreen,
                    uncheckedThumbColor = ShieldTextTertiary,
                    uncheckedTrackColor = ShieldSurfaceElevated,
                    uncheckedBorderColor = ShieldBorder
                )
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShieldSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ShieldAccentGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ShieldTextPrimary
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ShieldTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        if (checked != null && onCheckedChange != null) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF042111),
                    checkedTrackColor = ShieldAccentGreen,
                    uncheckedThumbColor = ShieldTextTertiary,
                    uncheckedTrackColor = ShieldSurfaceElevated,
                    uncheckedBorderColor = ShieldBorder
                )
            )
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ShieldTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseBottomSheet(
    onDismiss: () -> Unit,
    onSelectDuration: (minutes: Int) -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShieldSurfaceCard,
        contentColor = ShieldTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "সুরক্ষা বিরতি দিন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldTextPrimary
            )
            Text(
                text = "কত সময়ের জন্য ফিল্টারিং বন্ধ রাখতে চান তা বেছে নিন:",
                fontSize = 13.sp,
                color = ShieldTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            val durations = listOf(
                Pair("১৫ মিনিট", 15),
                Pair("৩০ মিনিট", 30),
                Pair("১ ঘণ্টা", 60),
                Pair("আজকের জন্য (২৪ ঘণ্টা)", 1440)
            )

            durations.forEach { (label, minutes) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectDuration(minutes)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            color = ShieldTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                            tint = ShieldWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ShieldBorder.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "বাতিল", onClick = onDismiss)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAllowlistBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (target: String, type: String, notes: String) -> Unit,
    sheetState: SheetState
) {
    var targetText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ওয়েবসাইট") }
    var notesText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShieldSurfaceCard,
        contentColor = ShieldTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Allowlist-এ যোগ করুন",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldTextPrimary
            )
            Text(
                text = "অনুমোদিত সাইট বা অ্যাপে DF Shield কোনো ফিল্টারিং করবে না।",
                fontSize = 13.sp,
                color = ShieldTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { selectedType = "ওয়েবসাইট" }
                ) {
                    RadioButton(
                        selected = selectedType == "ওয়েবসাইট",
                        onClick = { selectedType = "ওয়েবসাইট" },
                        colors = RadioButtonDefaults.colors(selectedColor = ShieldAccentGreen)
                    )
                    Text("ওয়েবসাইট ডোমেইন", fontSize = 14.sp, color = ShieldTextPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { selectedType = "অ্যাপ" }
                ) {
                    RadioButton(
                        selected = selectedType == "অ্যাপ",
                        onClick = { selectedType = "অ্যাপ" },
                        colors = RadioButtonDefaults.colors(selectedColor = ShieldAccentGreen)
                    )
                    Text("অ্যাপ প্যাকেজ", fontSize = 14.sp, color = ShieldTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it },
                label = { Text(if (selectedType == "ওয়েবসাইট") "যেমন: example.com" else "যেমন: com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldAccentGreen,
                    unfocusedBorderColor = ShieldBorder,
                    focusedLabelColor = ShieldAccentGreen,
                    unfocusedLabelColor = ShieldTextTertiary,
                    focusedTextColor = ShieldTextPrimary,
                    unfocusedTextColor = ShieldTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("নোট (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldAccentGreen,
                    unfocusedBorderColor = ShieldBorder,
                    focusedLabelColor = ShieldAccentGreen,
                    unfocusedLabelColor = ShieldTextTertiary,
                    focusedTextColor = ShieldTextPrimary,
                    unfocusedTextColor = ShieldTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "যোগ করুন",
                enabled = targetText.isNotBlank(),
                onClick = {
                    onAdd(targetText, selectedType, notesText)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(text = "বাতিল", onClick = onDismiss)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
