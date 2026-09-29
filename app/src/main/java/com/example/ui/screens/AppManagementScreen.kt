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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AppListItem
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary

@Composable
fun AppManagementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val apps by viewModel.appProtections.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("app_management_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যাপ সুরক্ষা",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                        Text(
                            text = "কোন অ্যাপের ট্রাফিক DF Shield ফিল্টার করবে",
                            fontSize = 12.sp,
                            color = ShieldTextSecondary
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("অ্যাপ খুঁজুন...", color = ShieldTextTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ShieldTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldAccentGreen,
                        unfocusedBorderColor = ShieldBorder,
                        focusedContainerColor = ShieldSurfaceCard,
                        unfocusedContainerColor = ShieldSurfaceCard,
                        focusedTextColor = ShieldTextPrimary,
                        unfocusedTextColor = ShieldTextPrimary
                    ),
                    singleLine = true
                )
            }

            // Quick Toggle Buttons: All ON / All OFF
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.setAllAppProtection(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldSurfaceElevated,
                            contentColor = ShieldAccentGreen
                        )
                    ) {
                        Text("সব নির্বাচন করুন", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.setAllAppProtection(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShieldTextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ShieldBorder)
                        )
                    ) {
                        Text("সব বন্ধ করুন", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // App Items
            items(filteredApps, key = { it.packageName }) { appItem ->
                AppListItem(
                    appName = appItem.appName,
                    packageName = appItem.packageName,
                    isProtected = appItem.isProtected,
                    onToggle = { isChecked ->
                        viewModel.toggleAppProtection(appItem.packageName, isChecked)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
