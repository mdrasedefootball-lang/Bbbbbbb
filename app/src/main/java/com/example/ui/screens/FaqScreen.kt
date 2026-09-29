package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.ShieldTextTertiary

data class FaqItem(val id: Int, val question: String, val answer: String)

@Composable
fun FaqScreen(
    onBack: () -> Unit
) {
    val faqList = listOf(
        FaqItem(
            id = 1,
            question = "DF Shield কী ব্লক করে?",
            answer = "বিজ্ঞাপন, পরিচিত ট্র্যাকার, অনুপ্রবেশকারী পপ-আপ, সন্দেহজনক ডোমেইন, ফিশিং আক্রমণ এবং অনাকাঙ্ক্ষিত ব্যাকগ্রাউন্ড ট্র্যাকিং সংযোগ।"
        ),
        FaqItem(
            id = 2,
            question = "সব বিজ্ঞাপন কি ব্লক হবে?",
            answer = "না। কিছু বিজ্ঞাপন প্রযুক্তিগত সীমাবদ্ধতা, হার্ডকোডেড ফার্স্ট-পার্টি এনক্রিপশন, অ্যাপের অভ্যন্তরীণ নেটওয়ার্কিং বা প্ল্যাটফর্মের নিয়মের কারণে ব্লক নাও হতে পারে। DF Shield স্বচ্ছতার সাথে সর্বোচ্চ কার্যকরী সুরক্ষা প্রদান করে।"
        ),
        FaqItem(
            id = 3,
            question = "কোনো সাইট কাজ না করলে কী করব?",
            answer = "১. সাইটটি Allowlist-এ যোগ করুন।\n২. ফিল্টার হালনাগাদ বা আপডেট করুন।\n৩. সাময়িকভাবে সুরক্ষা ১৫ মিনিটের জন্য বিরতি দিয়ে পরীক্ষা করুন।\n৪. প্রয়োজনে উন্নত নিয়ম পরীক্ষা করুন।"
        ),
        FaqItem(
            id = 4,
            question = "DF Shield কি আমার ডেটা সংগ্রহ করে?",
            answer = "না। DF Shield এর মূল দর্শন 'Privacy First'। সমস্ত ডিএনএস ফিল্টারিং এবং পরিসংখ্যান অন-ডিভাইস লোকাল মেমরিতে প্রক্রিয়াজাত হয়। কোনো ব্যক্তিগত তথ্য বাইরের সার্ভারে আপলোড করা হয় না।"
        ),
        FaqItem(
            id = 5,
            question = "VPN অনুমতি কেন প্রয়োজন?",
            answer = "Android অপারেটিং সিস্টেমে রুট এক্সেস ছাড়া অন-ডিভাইস লোকাল ডিএনএস ফিল্টারিং সক্রিয় করার একমাত্র নিরাপদ ও অনুমোদিত মাধ্যম হলো স্থানীয় VpnService ইন্টারফেস। এটি কোনো বাইরের রিমোট ভিপিএন প্রক্সি নয়, আপনার ডিভাইসেই লুপব্যাক চলে।"
        )
    )

    val expandedMap = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            put(1, true)
            put(2, true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("faq_screen")
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
                            text = "FAQ / সাহায্য",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                        Text(
                            text = "সাধারণ প্রশ্ন ও সমাধান",
                            fontSize = 12.sp,
                            color = ShieldTextSecondary
                        )
                    }
                }
            }

            items(faqList, key = { it.id }) { faq ->
                val isExpanded = expandedMap[faq.id] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expandedMap[faq.id] = !isExpanded },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isExpanded) ShieldAccentGreen.copy(alpha = 0.3f) else ShieldBorder
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
                            Text(
                                text = faq.question,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShieldTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ShieldTextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(ShieldBorder.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 13.sp,
                                    color = ShieldTextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
