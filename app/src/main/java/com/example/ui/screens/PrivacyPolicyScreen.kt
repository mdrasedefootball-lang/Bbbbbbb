package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("privacy_policy_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShieldTextPrimary
                    )
                    Text(
                        text = "গোপনীয়তা ও ডেটা সুরক্ষা নীতিমালা",
                        fontSize = 12.sp,
                        color = ShieldTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ShieldAccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আমাদের মূল প্রতিশ্রুতি",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "DF Shield ব্যবহারকারীর গোপনীয়তাকে সর্বোচ্চ অগ্রাধিকার দেয়। অ্যাপটি তৈরি করা হয়েছে এমনভাবে যাতে আপনার কোনো ব্যক্তিগত ডেটা, ব্রাউজিং রেকর্ড বা নেটওয়ার্ক ট্র্যাফিক আমাদের বা অন্য কোনো সার্ভারে প্রেরণ না হয়।",
                        fontSize = 13.sp,
                        color = ShieldTextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    PolicySection(
                        title = "১. লোকাল প্রসেসিং (Local-First)",
                        body = "সব ধরনের ট্র্যাকার ও অ্যাড ডোমেইন ফিল্টারিং আপনার ডিভাইসেই স্থানীয়ভাবে যাচাই করা হয়। কোনো ক্লাউড প্রক্সির মধ্য দিয়ে ট্র্যাফিক ঘুরিয়ে নেওয়া হয় না।"
                    )

                    PolicySection(
                        title = "২. VPN ইন্টারফেসের ব্যবহার",
                        body = "Android ডিভাইসে স্থানীয়ভাবে ডিএনএস ফিল্টারিংয়ের সুবিধা দিতে VpnService API ব্যবহৃত হয়। এটি আপনার ডিভাইসের ভেতরই সীমাবদ্ধ এবং ডেটা প্যাকেট বাইরে পাঠায় না।"
                    )

                    PolicySection(
                        title = "৩. লগ ও পরিসংখ্যান",
                        body = "ব্লক করা বিজ্ঞাপনের সংখ্যা বা ট্র্যাকার হিস্ট্রি শুধুমাত্র আপনার ডিভাইসের SQLite/Room ডেটাবেজে সংরক্ষিত থাকে।"
                    )

                    PolicySection(
                        title = "৪. যোগাযোগ ও অধিকার",
                        body = "ব্যবহারকারী যেকোনো সময় সেটিংস থেকে তার সমস্ত ডেটা এক ক্লিকে মুছে ফেলতে পারেন।"
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShieldAccentGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            color = ShieldTextSecondary,
            lineHeight = 18.sp
        )
    }
}
