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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AllowlistEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddAllowlistBottomSheet
import com.example.ui.components.AllowlistItem
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldDanger
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowlistScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.allowlistEntries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<AllowlistEntity?>(null) }

    var showAddSheet by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredItems = items.filter {
        it.target.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true)
    }

    if (showAddSheet) {
        AddAllowlistBottomSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { target, type, notes ->
                viewModel.addAllowlistEntry(target, type, notes)
            },
            sheetState = addSheetState
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = ShieldSurfaceCard,
            title = {
                Text(
                    text = "Allowlist থেকে সরাবেন?",
                    color = ShieldTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "\"${itemToDelete!!.target}\" সাইটটি Allowlist থেকে সরালে পুনরায় এতে DF Shield সুরক্ষা প্রযোজ্য হবে।",
                    color = ShieldTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.removeAllowlistEntry(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldDanger)
                ) {
                    Text("সরান", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemToDelete = null }
                ) {
                    Text("বাতিল", color = ShieldTextPrimary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("allowlist_screen")
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অনুমোদিত তালিকা",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                        Text(
                            text = "যেসব সাইট বা অ্যাপে সুরক্ষা বন্ধ রাখতে চান",
                            fontSize = 12.sp,
                            color = ShieldTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showAddSheet = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShieldAccentGreen)
                            .size(38.dp)
                            .testTag("add_allowlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "যোগ করুন",
                            tint = Color(0xFF042111)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("সাইট বা অ্যাপ খুঁজুন...", color = ShieldTextTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ShieldTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("allowlist_search_input"),
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

            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = ShieldTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "অনুমোদিত তালিকা খালি" else "কোন ফলাফল পাওয়া যায়নি",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ShieldTextSecondary
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "উপরের '+' বাটনে ট্যাপ করে সাইট যোগ করুন" else "বানান পরীক্ষা করুন বা অন্য কীওয়ার্ড লিখুন",
                            fontSize = 13.sp,
                            color = ShieldTextTertiary
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    AllowlistItem(
                        target = item.target,
                        type = item.type,
                        onDelete = { itemToDelete = item }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
