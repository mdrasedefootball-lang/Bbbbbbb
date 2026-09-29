package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import com.example.ui.theme.ShieldWarningContainer
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeSupportScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pendingUrl by viewModel.pendingYouTubeUrl.collectAsState()
    val totalBlocked by viewModel.youtubeAdsBlocked.collectAsState()

    var inputUrl by remember { mutableStateOf(pendingUrl ?: "") }
    var currentWebUrl by remember {
        mutableStateOf(
            if (!pendingUrl.isNullOrBlank()) {
                getCleanVideoUrl(pendingUrl!!)
            } else {
                "https://m.youtube.com"
            }
        )
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var adShieldActive by remember { mutableStateOf(true) }
    var sessionBlockedCount by remember { mutableIntStateOf(0) }
    var isBrowseMode by remember { mutableStateOf(true) }

    LaunchedEffect(pendingUrl) {
        if (!pendingUrl.isNullOrBlank()) {
            val target = getCleanVideoUrl(pendingUrl!!)
            inputUrl = pendingUrl!!
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
        "static.doubleclick.net"
    )

    val adBlockScript = """
        (function() {
            function removeAds() {
                // 1. Click skip ad buttons immediately
                var skipButtons = document.querySelectorAll(
                    '.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton, .ytp-ad-overlay-close-button, .ytm-ad-overlay-close-button'
                );
                for (var i = 0; i < skipButtons.length; i++) {
                    skipButtons[i].click();
                }

                // 2. Fast forward video ads
                var video = document.querySelector('video');
                var adShowing = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay');
                if (video && adShowing) {
                    video.muted = true;
                    if (video.duration && isFinite(video.duration)) {
                        video.currentTime = video.duration;
                    }
                    video.playbackRate = 16.0;
                }

                // 3. Hide all ad containers & sponsored elements
                var adSelectors = [
                    '.ytp-ad-overlay-container', '.ytp-ad-module', '.video-ads',
                    'ytd-promoted-sparkles-web-renderer', 'ytd-display-ad-renderer',
                    'ytd-in-feed-ad-layout-renderer', 'ytd-banner-promo-renderer',
                    'ytd-ad-slot-renderer', '#player-ads', '.sparkles-light-cta',
                    'ytd-promoted-video-renderer', 'ytd-compact-promoted-video-renderer',
                    'ytm-promoted-sparkles-web-renderer', 'ytm-companion-ad-renderer',
                    'ytm-ad-slot-renderer', 'ytm-promoted-video-renderer'
                ];
                for (var s = 0; s < adSelectors.length; s++) {
                    var els = document.querySelectorAll(adSelectors[s]);
                    for (var j = 0; j < els.length; j++) {
                        els[j].style.display = 'none';
                        els[j].remove();
                    }
                }
            }

            // Run repeatedly to catch dynamic ads
            if (!window.__dfShieldInstalled) {
                window.__dfShieldInstalled = true;
                setInterval(removeAds, 50);

                try {
                    var observer = new MutationObserver(function() {
                        removeAds();
                    });
                    if (document.body) {
                        observer.observe(document.body, { childList: true, subtree: true });
                    }
                } catch(e) {}

                var style = document.createElement('style');
                style.textContent = `
                    .video-ads, .ytp-ad-module, .ytp-ad-overlay-container,
                    .ytp-ad-player-overlay, ytd-promoted-sparkles-web-renderer,
                    ytd-display-ad-renderer, ytd-ad-slot-renderer, #player-ads,
                    .sparkles-light-cta, ytd-promoted-video-renderer,
                    ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer,
                    ytm-ad-slot-renderer { display: none !important; }
                `;
                document.head.appendChild(style);
            }
            removeAds();
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            text = "YouTube সুরক্ষা",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ShieldAccentGreenContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "বিজ্ঞাপন ব্লক সক্রিয়",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldAccentGreen
                            )
                        }
                    }
                    Text(
                        text = "ভিডিও প্রি-রোল ও ব্যানার অ্যাড দমন মোড",
                        fontSize = 11.sp,
                        color = ShieldTextSecondary
                    )
                }

                // Refresh button
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

            // Status & Counter Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ShieldAccentGreen.copy(alpha = 0.35f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ShieldAccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DF Shield Ad-Suppressor",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShieldTextPrimary
                            )
                            Text(
                                text = "ব্লক করা বিজ্ঞাপন: ${totalBlocked + sessionBlockedCount}টি",
                                fontSize = 11.sp,
                                color = ShieldAccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                    placeholder = { Text("ভিডিও লিংক বা সার্চ লিখুন...", fontSize = 12.sp, color = ShieldTextTertiary) },
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
                        .clip(RoundedCornerShape(12.dp)),
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
                                                // Return empty response to drop the ad request
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

private fun getCleanVideoUrl(input: String): String {
    val trimmed = input.trim()
    val id = extractYouTubeId(trimmed)
    return if (id != null) {
        "https://www.youtube-nocookie.com/embed/$id?autoplay=1&rel=0&iv_load_policy=3"
    } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://m.youtube.com/results?search_query=" + Uri.encode(trimmed)
    }
}

private fun extractYouTubeId(url: String): String? {
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=)[^#\\&\\?\\n]*"
    val compiledPattern = java.util.regex.Pattern.compile(pattern)
    val matcher = compiledPattern.matcher(url)
    return if (matcher.find()) {
        matcher.group()
    } else null
}
