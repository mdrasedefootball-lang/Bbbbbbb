package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel
import com.example.ui.theme.ShieldAccentGreen
import com.example.ui.theme.ShieldAccentGreenContainer
import com.example.ui.theme.ShieldBackground
import com.example.ui.theme.ShieldBorder
import com.example.ui.theme.ShieldSurfaceCard
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldTextPrimary
import com.example.ui.theme.ShieldTextSecondary
import com.example.ui.theme.ShieldTextTertiary
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FacebookShieldScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val totalBlocked by viewModel.facebookSponsoredBlocked.collectAsState()
    var sessionBlockedCount by remember { mutableIntStateOf(0) }
    var shieldActive by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val fbAdDomains = listOf(
        "an.facebook.com",
        "pixel.facebook.com",
        "connect.facebook.net",
        "graph.facebook.com/network_ads",
        "creative.ak.fbcdn.net",
        "ad.atdmt.com"
    )

    // Script to scan & remove sponsored posts and ads
    val fbSponsorBlockScript = """
        (function() {
            function removeSponsoredPosts() {
                // Find all article / story blocks
                var stories = document.querySelectorAll('article, [data-ft], [role="article"], div[data-tracking-duration-id]');
                for (var i = 0; i < stories.length; i++) {
                    var el = stories[i];
                    var text = el.innerText || el.textContent || '';
                    var aria = el.getAttribute('aria-label') || '';
                    
                    // Check for Sponsored / স্পনসরড keywords
                    if (
                        text.indexOf('Sponsored') !== -1 ||
                        text.indexOf('স্পনসরড') !== -1 ||
                        aria.indexOf('Sponsored') !== -1 ||
                        aria.indexOf('স্পনসরড') !== -1 ||
                        el.querySelector('a[href*="/ads/about"]') !== null ||
                        el.querySelector('[aria-label*="Sponsored"]') !== null
                    ) {
                        el.style.display = 'none';
                        el.remove();
                    }
                }

                // Hide generic ad containers
                var adSelectors = [
                    '.fbAdUnit', '[data-ad-slot]', '[data-ad-preview]',
                    '.sponsored_indicator', 'iframe[src*="facebook.com/plugins"]'
                ];
                for (var s = 0; s < adSelectors.length; s++) {
                    var ads = document.querySelectorAll(adSelectors[s]);
                    for (var j = 0; j < ads.length; j++) {
                        ads[j].style.display = 'none';
                        ads[j].remove();
                    }
                }
            }

            if (!window.__fbShieldInstalled) {
                window.__fbShieldInstalled = true;
                setInterval(removeSponsoredPosts, 150);

                try {
                    var observer = new MutationObserver(function() {
                        removeSponsoredPosts();
                    });
                    if (document.body) {
                        observer.observe(document.body, { childList: true, subtree: true });
                    }
                } catch(e) {}

                var style = document.createElement('style');
                style.textContent = `
                    .fbAdUnit, [data-ad-slot], [data-ad-preview],
                    .sponsored_indicator { display: none !important; }
                `;
                document.head.appendChild(style);
            }
            removeSponsoredPosts();
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
            .testTag("facebook_shield_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            text = "Facebook স্পনসর সুরক্ষা",
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
                                text = "স্পনসরড পোস্ট ব্লক",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldAccentGreen
                            )
                        }
                    }
                    Text(
                        text = "ফিডের অবাঞ্ছিত স্পনসরড পোস্ট ও ভিডিও বিজ্ঞাপন অপসারণ",
                        fontSize = 11.sp,
                        color = ShieldTextSecondary
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

            // Status Card
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
                                text = "Facebook Sponsor Filter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShieldTextPrimary
                            )
                            Text(
                                text = "ব্লক করা স্পনসরড উপাদান: ${totalBlocked + sessionBlockedCount}টি",
                                fontSize = 11.sp,
                                color = ShieldAccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (shieldActive) "সক্রিয়" else "নিষ্ক্রিয়",
                            fontSize = 11.sp,
                            color = if (shieldActive) ShieldAccentGreen else ShieldTextTertiary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = shieldActive,
                            onCheckedChange = { shieldActive = it },
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

            // WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ShieldBackground)
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
                                    if (shieldActive) {
                                        for (domain in fbAdDomains) {
                                            if (url.contains(domain, ignoreCase = true)) {
                                                view?.post {
                                                    sessionBlockedCount++
                                                }
                                                viewModel.recordFacebookBlocked()
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
                                    if (shieldActive) {
                                        view?.evaluateJavascript(fbSponsorBlockScript, null)
                                    }
                                }

                                override fun onLoadResource(view: WebView?, url: String?) {
                                    super.onLoadResource(view, url)
                                    if (shieldActive) {
                                        view?.evaluateJavascript(fbSponsorBlockScript, null)
                                    }
                                }
                            }

                            loadUrl("https://m.facebook.com")
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
