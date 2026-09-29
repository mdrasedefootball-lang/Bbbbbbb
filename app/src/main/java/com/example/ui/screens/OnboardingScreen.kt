package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldAccentGreenContainer
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("onboarding_screen")
    ) {
        if (step == 0) {
            OnboardingWelcomeView(
                onStart = { step = 1 },
                onSkip = onComplete
            )
        } else {
            OnboardingSetupView(
                onFinish = onComplete
            )
        }
    }
}

@Composable
private fun OnboardingWelcomeView(
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Hero Emblem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(ShieldAccentGreenContainer.copy(alpha = 0.6f))
                    .border(2.dp, ShieldAccentGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "DF Shield",
                    tint = ShieldAccentGreen,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "DF Shield",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "আপনার ডিজিটাল গোপনীয়তার জন্য সহজ সুরক্ষা।",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = ShieldAccentGreen,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "বিজ্ঞাপন, ট্র্যাকার এবং সন্দেহজনক ওয়েবসাইট থেকে আপনার ডিভাইসকে সুরক্ষিত রাখতে DF Shield ব্যবহার করুন।",
                fontSize = 14.sp,
                color = ShieldTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                text = "শুরু করুন",
                onClick = onStart,
                testTag = "onboarding_start_button"
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton(
                text = "পরে সেটআপ করব",
                onClick = onSkip,
                testTag = "onboarding_skip_button"
            )
        }
    }
}

@Composable
private fun OnboardingSetupView(
    onFinish: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "সুরক্ষা সেটআপ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ShieldTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "DF Shield আপনার ডিভাইসে যেভাবে কাজ করে:",
            fontSize = 14.sp,
            color = ShieldTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        SetupCard(
            title = "বিজ্ঞাপন ব্লক",
            desc = "ওয়েবসাইটের বিরক্তিকর বিজ্ঞাপন ও পপ-আপ কমাতে সাহায্য করে।",
            icon = Icons.Default.Block,
            iconTint = ShieldAccentGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        SetupCard(
            title = "ট্র্যাকার সুরক্ষা",
            desc = "পরিচিত ট্র্যাকার ও অনাকাঙ্ক্ষিত সংযোগ ব্লক করতে সাহায্য করে।",
            icon = Icons.Default.Fingerprint,
            iconTint = ShieldAccentGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        SetupCard(
            title = "নিরাপত্তা সুরক্ষা",
            desc = "পরিচিত সন্দেহজনক বা ক্ষতিকর ডোমেইন শনাক্ত করতে সাহায্য করে।",
            icon = Icons.Default.Security,
            iconTint = ShieldAccentGreen
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy Guarantee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ShieldSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ShieldAccentGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ShieldAccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "গোপনীয়তার প্রতিশ্রুতি",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "আপনার ব্রাউজিং ডেটা আমাদের সার্ভারে পাঠানোর প্রয়োজন নেই। সম্পূর্ণ লোকাল প্রসেসিং।",
                        fontSize = 12.sp,
                        color = ShieldTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "সুরক্ষা সেটআপ করুন",
            onClick = onFinish,
            testTag = "onboarding_setup_cta"
        )
    }
}

@Composable
private fun SetupCard(
    title: String,
    desc: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShieldSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ShieldTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = ShieldTextSecondary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
