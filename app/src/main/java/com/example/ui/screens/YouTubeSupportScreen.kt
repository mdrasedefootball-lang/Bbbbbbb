package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.service.YouTubeAdSkipAccessibilityService
import com.example.ui.MainViewModel
import com.example.ui.components.PrimaryButton
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
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeSupportScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalBlocked by viewModel.youtubeAdsBlocked.collectAsState()
    val pendingUrl by viewModel.pendingYouTubeUrl.collectAsState()

    var adShieldActive by remember { mutableStateOf(true) }
    var currentWebUrl by remember { mutableStateOf("https://m.youtube.com") }
    var inputUrl by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var sessionBlockedCount by remember { mutableIntStateOf(0) }
    var isAutoSkipActive by remember { mutableStateOf(YouTubeAdSkipAccessibilityService.isServiceRunning) }

    fun getCleanVideoUrl(rawUrl: String): String {
        return try {
            val uri = Uri.parse(rawUrl.trim())
            when {
                rawUrl.contains("youtu.be/") -> {
                    val id = uri.lastPathSegment
                    if (!id.isNullOrEmpty()) "https://m.youtube.com/watch?v=$id" else rawUrl
                }
                rawUrl.contains("youtube.com/shorts/") -> {
                    val id = uri.lastPathSegment
                    if (!id.isNullOrEmpty()) "https://m.youtube.com/watch?v=$id" else rawUrl
                }
                rawUrl.contains("youtube.com/watch") -> {
                    val id = uri.getQueryParameter("v")
                    if (!id.isNullOrEmpty()) "https://m.youtube.com/watch?v=$id" else rawUrl
                }
                rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
                rawUrl.isNotBlank() -> "https://m.youtube.com/results?search_query=${Uri.encode(rawUrl)}"
                else -> "https://m.youtube.com"
            }
        } catch (_: Exception) {
            rawUrl
        }
    }

    LaunchedEffect(pendingUrl) {
        if (!pendingUrl.isNullOrBlank()) {
            val target = getCleanVideoUrl(pendingUrl!!)
            viewModel.clearPendingYouTubeUrl()
            currentWebUrl = target
            webViewRef?.loadUrl(target)
        }
    }

    val adDomains = listOf(
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "doubleclick.net",
        "youtube.com/api/stats/ads",
        "youtube.com/pagead",
        "youtube.com/ptracking",
        "youtube.com/api/stats/qoe",
        "static.doubleclick.net",
        "youtube.com/get_midroll_info"
    )

    // Ultimate uBlock Origin + SponsorBlock Engine for YouTube
    val adBlockScript = """
        (function() {
            if (window.__dfShieldInstalled) return;
            window.__dfShieldInstalled = true;

            // 1. Hook JSON.parse to remove ad placements before player config loads
            var origParse = JSON.parse;
            JSON.parse = function() {
                var res = origParse.apply(this, arguments);
                try {
                    if (res && typeof res === 'object') {
                        if (res.adPlacements) res.adPlacements = [];
                        if (res.playerAds) res.playerAds = [];
                        if (res.adSlots) res.adSlots = [];
                    }
                } catch(e) {}
                return res;
            };

            // 2. Intercept fetch & XHR to drop ad tracking
            var adUrls = [
                '/api/stats/ads', '/pagead/', 'googleads.g.doubleclick.net',
                '/ptracking', '/get_midroll_info', 'static.doubleclick.net'
            ];
            function isAdUrl(u) {
                if (!u) return false;
                var str = String(u).toLowerCase();
                for (var i = 0; i < adUrls.length; i++) {
                    if (str.indexOf(adUrls[i]) !== -1) return true;
                }
                return false;
            }

            var origFetch = window.fetch;
            if (origFetch) {
                window.fetch = function(input, init) {
                    var u = (typeof input === 'string') ? input : (input && input.url ? input.url : '');
                    if (isAdUrl(u)) {
                        return Promise.resolve(new Response(JSON.stringify({}), { status: 200 }));
                    }
                    return origFetch.apply(this, arguments);
                };
            }

            // 3. Fast auto-skip & ad suppressor loop (every 30ms)
            function executeAutoSkip() {
                // Click skip button immediately
                var skipButtons = document.querySelectorAll(
                    '.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton, ' +
                    '.ytp-skip-ad-button, .ytp-ad-skip-button-text, .ytp-ad-overlay-close-button, ' +
                    '.ytm-ad-overlay-close-button'
                );
                for (var i = 0; i < skipButtons.length; i++) {
                    try { skipButtons[i].click(); } catch(e) {}
                }

                // If ad is playing: fast forward to end and mute
                var video = document.querySelector('video');
                var adShowing = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay');
                if (video && adShowing) {
                    video.muted = true;
                    video.playbackRate = 16.0;
                    if (video.duration && isFinite(video.duration) && video.duration > 0) {
                        video.currentTime = video.duration;
                    }
                }

                // Remove banner ads and sponsored shelves
                var adSelectors = [
                    '.ytp-ad-overlay-container', '.ytp-ad-module', '.video-ads',
                    'ytd-promoted-sparkles-web-renderer', 'ytd-display-ad-renderer',
                    'ytd-in-feed-ad-layout-renderer', 'ytd-banner-promo-renderer',
                    'ytd-ad-slot-renderer', '#player-ads', '.sparkles-light-cta',
                    'ytd-promoted-video-renderer', 'ytd-compact-promoted-video-renderer',
                    'ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer',
                    'ytm-ad-slot-renderer', 'ytm-promoted-video-renderer', '.ytp-ad-message-container'
                ];
                for (var s = 0; s < adSelectors.length; s++) {
                    var els = document.querySelectorAll(adSelectors[s]);
                    for (var j = 0; j < els.length; j++) {
                        els[j].style.display = 'none';
                        try { els[j].remove(); } catch(e) {}
                    }
                }
            }

            setInterval(executeAutoSkip, 30);

            // 4. Cosmetic style injection
            var style = document.createElement('style');
            style.textContent = `
                .video-ads, .ytp-ad-module, .ytp-ad-overlay-container,
                .ytp-ad-player-overlay, ytd-promoted-sparkles-web-renderer,
                ytd-display-ad-renderer, ytd-ad-slot-renderer, #player-ads,
                .sparkles-light-cta, ytd-promoted-video-renderer,
                ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer,
                ytm-ad-slot-renderer, .ytp-ad-message-container,
                ytd-in-feed-ad-layout-renderer { display: none !important; opacity: 0 !important; }
            `;
            (document.head || document.documentElement).appendChild(style);

            executeAutoSkip();
        })();
    """.trimIndent()

    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShieldBackground)
            .testTag("youtube_support_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "YouTube সুরক্ষা ও অটো-স্কিপ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                    }
                    Text(
                        text = "uBlock + AdGuard হাইব্রিড অ্যাড ব্লকার",
                        fontSize = 11.sp,
                        color = ShieldAccentGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { webViewRef?.reload() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShieldSurfaceElevated)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "রিলোড",
                        tint = ShieldTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Official YouTube App Auto-Skip Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF0000).copy(alpha = 0.4f))
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF0000).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFFFF0000),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "অফিসিয়াল YouTube অ্যাপে অটো-স্কিপ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldTextPrimary
                                )
                                Text(
                                    text = "আসল YouTube অ্যাপে বিজ্ঞাপন আসা মাত্রই স্কিপ হবে",
                                    fontSize = 11.sp,
                                    color = ShieldTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isAutoSkipActive) ShieldAccentGreenContainer
                                    else ShieldWarning.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAutoSkipActive) "সক্রিয় ✓" else "অনুমতি প্রয়োজন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoSkipActive) ShieldAccentGreen else ShieldWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট স্কিপ করা বিজ্ঞাপন: ${totalBlocked + sessionBlockedCount}টি",
                            fontSize = 11.sp,
                            color = ShieldAccentGreen,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAutoSkipActive) ShieldSurfaceElevated else Color(0xFFFF0000),
                                contentColor = if (isAutoSkipActive) ShieldTextPrimary else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAutoSkipActive) "সেটিংস চেক" else "অটো-স্কিপ চালু করুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // In-app Video Player Status & Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ShieldAccentGreen.copy(alpha = 0.35f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ShieldAccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ইন-অ্যাপ ক্লিন প্লেয়ার (uBlock Engine)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShieldTextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (adShieldActive) "চালু" else "বন্ধ",
                            fontSize = 11.sp,
                            color = if (adShieldActive) ShieldAccentGreen else ShieldTextTertiary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = adShieldActive,
                            onCheckedChange = { adShieldActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF042111),
                                checkedTrackColor = ShieldAccentGreen,
                                uncheckedThumbColor = ShieldTextTertiary,
                                uncheckedTrackColor = ShieldSurfaceElevated,
                                uncheckedBorderColor = ShieldBorder
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // URL Search / Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("ভিডিও লিংক পেস্ট করুন বা সার্চ লিখুন...", fontSize = 12.sp, color = ShieldTextTertiary) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("youtube_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldAccentGreen,
                        unfocusedBorderColor = ShieldBorder,
                        focusedContainerColor = ShieldSurfaceCard,
                        unfocusedContainerColor = ShieldSurfaceCard,
                        focusedTextColor = ShieldTextPrimary,
                        unfocusedTextColor = ShieldTextPrimary
                    )
                )

                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            val target = getCleanVideoUrl(inputUrl)
                            currentWebUrl = target
                            webViewRef?.loadUrl(target)
                        }
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("youtube_go_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShieldAccentGreen,
                        contentColor = Color(0xFF042111)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "চালান",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // WebView Video Player & Shielded Web Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                            }

                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.startsWith("http://") || url.startsWith("https://")) {
                                        return false
                                    }
                                    return true
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val url = request?.url?.toString() ?: ""
                                    if (adShieldActive) {
                                        for (domain in adDomains) {
                                            if (url.contains(domain, ignoreCase = true)) {
                                                view?.post {
                                                    sessionBlockedCount++
                                                }
                                                viewModel.recordYouTubeBlocked()
                                                return WebResourceResponse(
                                                    "text/plain",
                                                    "UTF-8",
                                                    ByteArrayInputStream(ByteArray(0))
                                                )
                                            }
                                        }
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (adShieldActive) {
                                        view?.evaluateJavascript(adBlockScript, null)
                                    }
                                }

                                override fun onLoadResource(view: WebView?, url: String?) {
                                    super.onLoadResource(view, url)
                                    if (adShieldActive) {
                                        view?.evaluateJavascript(adBlockScript, null)
                                    }
                                }
                            }

                            loadUrl(currentWebUrl)
                            webViewRef = this
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
