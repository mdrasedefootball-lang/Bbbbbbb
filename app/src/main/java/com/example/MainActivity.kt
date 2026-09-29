package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AllowlistScreen
import com.example.ui.screens.AppManagementScreen
import com.example.ui.screens.FacebookShieldScreen
import com.example.ui.screens.FaqScreen
import com.example.ui.screens.FiltersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.YouTubeSupportScreen
import com.example.ui.theme.DFShieldTheme
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurface
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary

class MainActivity : ComponentActivity() {

    private var activeViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            activeViewModel = viewModel

            // Handle initial launch intent if shared from YouTube
            LaunchedEffect(Unit) {
                if (intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    viewModel.handleSharedYouTubeUrl(sharedText)
                }
            }

            DFShieldTheme {
                DFShieldApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            activeViewModel?.handleSharedYouTubeUrl(sharedText)
        }
    }
}

@Composable
fun DFShieldApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()

    // VPN Permission contract launcher
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.vpnPermissionIntent.collect { intent ->
            vpnLauncher.launch(intent)
        }
    }

    val isTopLevelScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.Statistics ||
            currentScreen is Screen.Filters ||
            currentScreen is Screen.Settings

    val isOnboarding = currentScreen is Screen.OnboardingWelcome || currentScreen is Screen.OnboardingSetup

    // Handle back button on secondary screens
    if (!isOnboarding && !isTopLevelScreen) {
        BackHandler {
            viewModel.navigateBack()
        }
    } else if (isTopLevelScreen && currentScreen !is Screen.Home) {
        BackHandler {
            viewModel.switchBottomTab(Screen.Home)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ShieldBackground,
        bottomBar = {
            if (!isOnboarding && isTopLevelScreen) {
                DFShieldBottomNav(
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        viewModel.switchBottomTab(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.OnboardingWelcome, Screen.OnboardingSetup -> {
                        OnboardingScreen(
                            onComplete = { viewModel.completeOnboarding() }
                        )
                    }
                    Screen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    }
                    Screen.Statistics -> {
                        StatisticsScreen(viewModel = viewModel)
                    }
                    Screen.Filters -> {
                        FiltersScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.switchBottomTab(Screen.Home) }
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    }
                    Screen.Allowlist -> {
                        AllowlistScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    Screen.AppManagement -> {
                        AppManagementScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    Screen.YouTubeSupport -> {
                        YouTubeSupportScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    Screen.FacebookShield -> {
                        FacebookShieldScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    Screen.FaqHelp -> {
                        FaqScreen(
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    Screen.PrivacyPolicy -> {
                        PrivacyPolicyScreen(
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DFShieldBottomNav(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .background(ShieldSurface)
            .border(
                width = 1.dp,
                color = ShieldBorder,
                shape = androidx.compose.ui.graphics.RectangleShape
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = ShieldSurface,
            tonalElevation = 0.dp,
            modifier = Modifier.testTag("df_bottom_navigation")
        ) {
            val navItems = listOf(
                Triple(Screen.Home, "হোম", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                Triple(Screen.Statistics, "পরিসংখ্যান", Pair(Icons.Filled.BarChart, Icons.Outlined.BarChart)),
                Triple(Screen.Filters, "ফিল্টার", Pair(Icons.Filled.FilterList, Icons.Outlined.FilterList)),
                Triple(Screen.Settings, "সেটিংস", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
            )

            navItems.forEach { (screen, label, icons) ->
                val selected = currentScreen == screen
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(screen) },
                    icon = {
                        Icon(
                            imageVector = if (selected) icons.first else icons.second,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF042111),
                        selectedTextColor = ShieldAccentGreen,
                        indicatorColor = ShieldAccentGreen,
                        unselectedIconColor = ShieldTextSecondary,
                        unselectedTextColor = ShieldTextTertiary
                    ),
                    modifier = Modifier.testTag("nav_tab_${label}")
                )
            }
        }
    }
}
