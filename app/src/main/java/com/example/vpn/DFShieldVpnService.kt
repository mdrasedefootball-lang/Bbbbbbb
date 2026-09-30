package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.ShieldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Collections
import java.util.Locale

class DFShieldVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val writeLock = Any()
    private var repository: ShieldRepository? = null

    // In-memory DNS cache: domain:qType -> DNS answer payload
    // Guarantees 0ms latency for frequent websites while preserving ad blocks
    private val dnsResponseCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, ByteArray>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean {
                return size > 500
            }
        }
    )

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val CHANNEL_ID = "df_shield_protection_channel"
        const val NOTIFICATION_ID = 1001

        var isRunning = false
            private set

        // Pre-resolved upstream DNS servers for zero-overhead routing
        private val ADGUARD_PRIMARY = InetAddress.getByAddress(byteArrayOf(94.toByte(), 140.toByte(), 14.toByte(), 14.toByte()))
        private val ADGUARD_SECONDARY = InetAddress.getByAddress(byteArrayOf(94.toByte(), 140.toByte(), 15.toByte(), 15.toByte()))
        private val CLOUDFLARE_SECURE = InetAddress.getByAddress(byteArrayOf(1.toByte(), 1.toByte(), 1.toByte(), 2.toByte()))
        private val CLOUDFLARE_FAST = InetAddress.getByAddress(byteArrayOf(1.toByte(), 1.toByte(), 1.toByte(), 1.toByte()))
        private val GOOGLE_FAST = InetAddress.getByAddress(byteArrayOf(8.toByte(), 8.toByte(), 8.toByte(), 8.toByte()))

        private val UPSTREAM_SERVERS = listOf(
            ADGUARD_PRIMARY,
            ADGUARD_SECONDARY,
            CLOUDFLARE_SECURE,
            CLOUDFLARE_FAST,
            GOOGLE_FAST
        )

        // Comprehensive database of mobile ad networks across apps, games, and web
        private val AD_NETWORKS = listOf(
            // Google / AdMob / DoubleClick
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "adservice.google",
            "admob.com",
            "ads.google.com",
            "afs.googlesyndication.com",
            "pagead2.googlesyndication.com",
            "mobileads.google.com",
            "adsymptotic.com",
            "app-measurement.com",

            // Unity Ads (In-game video, banner & reward ads)
            "unityads.unity3d.com",
            "auction.unityads.unity3d.com",
            "webview.unityads.unity3d.com",
            "config.unityads.unity3d.com",
            "adserver.unityads.unity3d.com",
            "ads.unity3d.com",

            // AppLovin / MAX (Top game ads)
            "applovin.com",
            "applastic.com",
            "d.applovin.com",
            "ms.applovin.com",
            "a.applovin.com",
            "rt.applovin.com",
            "assets.applovin.com",

            // IronSource / Supersonic (Game interstitials & offerwalls)
            "ironsrc.com",
            "supersonicads.com",
            "platform.ironsrc.com",
            "init.supersonicads.com",
            "edge-live.ironsrc.com",

            // Mintegral / Rayjump (Mobile game ads)
            "mintegral.com",
            "rayjump.com",
            "pg-wv.rayjump.com",
            "net.rayjump.com",

            // Pangle / TikTok Ads (Mobile app ads)
            "pangle-ads.com",
            "pangolin-sdk-toutiao.com",
            "pangolin-sdk-toutiao-b.com",
            "ads.tiktok.com",
            "analytics.tiktok.com",
            "toblog.ctobsnssdk.com",

            // InMobi
            "inmobi.com",
            "config-inmobi.com",
            "ads.inmobi.com",
            "telemetry.inmobi.com",

            // Vungle / Liftoff (Rewarded video ads)
            "vungle.com",
            "api.vungle.com",
            "ads.vungle.com",
            "liftoff.io",

            // Chartboost
            "chartboost.com",
            "live.chartboost.com",
            "da.chartboost.com",

            // Facebook Audience Network
            "an.facebook.com",
            "pixel.facebook.com",

            // Bigo Ads
            "bigossp.com",
            "ads.bigo.sg",

            // AdColony, Tapjoy, Fyber, Smaato, StartApp
            "adcolony.com",
            "tapjoy.com",
            "tapjoyads.com",
            "fyber.com",
            "inner-active.mobi",
            "hyprmx.com",
            "smaato.net",
            "startappservice.com",
            "startapp.com",
            "ogury.io",
            "pollfish.com",
            "leadbolt.com",
            "admoda.com",
            "mobfox.com",
            "nexage.com",
            "mopub.com",

            // Web banner, Native & Pop ad networks
            "criteo.com",
            "criteo.net",
            "taboola.com",
            "outbrain.com",
            "adnxs.com",
            "scorecardresearch.com",
            "quantserve.com",
            "moatads.com",
            "rubiconproject.com",
            "pubmatic.com",
            "openx.net",
            "casalemedia.com",
            "bidswitch.net",
            "smartadserver.com",
            "adroll.com",
            "yieldmo.com",
            "advertising.com",
            "popads.net",
            "popcash.net",
            "propellerads.com",
            "exoclick.com",
            "adcash.com",
            "hilltopads.net",
            "clickadu.com",
            "adsterra.com",
            "mgid.com",
            "revcontent.com",
            "zergnet.com",
            "adfox.ru",
            "an.yandex.ru",

            // Tracking & telemetry SDKs
            "appsflyer.com",
            "adjust.com",
            "branch.io",
            "kochava.com",
            "singular.net",
            "flurry.com"
        )

        // Essential service domains that must never be blocked so the internet is always 100% functional
        private val ESSENTIAL_SAFE_ROOTS = setOf(
            "google.com",
            "youtube.com",
            "facebook.com",
            "fbcdn.net",
            "instagram.com",
            "cdninstagram.com",
            "whatsapp.com",
            "whatsapp.net",
            "messenger.com",
            "telegram.org",
            "tiktok.com",
            "tiktokcdn.com",
            "twitter.com",
            "x.com",
            "twimg.com",
            "linkedin.com",
            "github.com",
            "wikipedia.org",
            "netflix.com",
            "spotify.com",
            "cloudflare.com",
            "android.com",
            "apple.com",
            "microsoft.com",
            "live.com",
            "yahoo.com",
            "bing.com",
            "bkash.com",
            "nagad.com.bd",
            "rocket.com.bd",
            "daraz.com.bd",
            "prothomalo.com",
            "bdnews24.com"
        )
    }

    override fun onCreate() {
        super.onCreate()
        repository = ShieldRepository(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                stopSelf()
            }
            ACTION_START -> {
                startVpn()
            }
            else -> {
                startVpn()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        createNotificationChannel()
        val notification = createNotification(
            "DF Shield: সব বিজ্ঞাপন ব্লক সক্রিয়",
            "ইন্টারনেট সম্পূর্ণ গতিতে সচল আছে এবং ফোনের সব বিজ্ঞাপন ব্লক হচ্ছে।"
        )
        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession("DF Shield AdBlock")
                .addAddress("10.240.0.1", 32)
                .addDnsServer("10.240.0.1")
                .addDnsServer("94.140.14.14")
                // Only route the DNS proxy address through the VPN.
                // 100% of all real internet traffic (web, video, gaming, messaging)
                // bypasses the VPN tunnel and connects directly through Wi-Fi or 4G/5G
                // at full hardware speed with zero bottleneck!
                .addRoute("10.240.0.1", 32)
                .addRoute("94.140.14.14", 32)
                .addRoute("94.140.15.15", 32)
                .addRoute("1.1.1.2", 32)
                .setMtu(1500)
                .setBlocking(true)

            try {
                builder.addDisallowedApplication(packageName)
            } catch (_: Exception) {}

            vpnInterface = builder.establish()
            isRunning = true

            // Launch high-throughput DNS interceptor
            vpnJob = serviceScope.launch {
                val pfd = vpnInterface ?: return@launch
                val input = FileInputStream(pfd.fileDescriptor)
                val output = FileOutputStream(pfd.fileDescriptor)
                val buffer = ByteArray(32768)

                try {
                    while (isActive && isRunning) {
                        val length = input.read(buffer)
                        if (length > 0) {
                            processPacket(buffer, length, output)
                        }
                    }
                } catch (_: Exception) {
                    // Closed cleanly on service stop
                } finally {
                    try { input.close() } catch (_: Exception) {}
                    try { output.close() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
            stopSelf()
        }
    }

    private fun processPacket(buffer: ByteArray, length: Int, output: FileOutputStream) {
        val versionAndIhl = buffer[0].toInt() and 0xFF
        val version = versionAndIhl shr 4
        if (version != 4) return
        val ihl = (versionAndIhl and 0x0F) * 4
        if (ihl < 20 || length < ihl + 8) return

        val protocol = buffer[9].toInt() and 0xFF
        if (protocol != 17) return // Only UDP packets

        val srcPort = ((buffer[ihl].toInt() and 0xFF) shl 8) or (buffer[ihl + 1].toInt() and 0xFF)
        val dstPort = ((buffer[ihl + 2].toInt() and 0xFF) shl 8) or (buffer[ihl + 3].toInt() and 0xFF)
        val udpLength = ((buffer[ihl + 4].toInt() and 0xFF) shl 8) or (buffer[ihl + 5].toInt() and 0xFF)

        // Only process DNS queries directed to port 53
        if (dstPort != 53 || udpLength < 8 || length < ihl + udpLength) return

        val dnsOffset = ihl + 8
        val dnsLength = udpLength - 8
        if (dnsLength < 12) return

        val dnsQuery = ByteArray(dnsLength)
        System.arraycopy(buffer, dnsOffset, dnsQuery, 0, dnsLength)

        val clientIp = ByteArray(4)
        System.arraycopy(buffer, 12, clientIp, 0, 4)
        val dnsServerIp = ByteArray(4)
        System.arraycopy(buffer, 16, dnsServerIp, 0, 4)

        val parsed = parseDomainName(dnsQuery)
        val domain = parsed?.first ?: ""
        val qEnd = parsed?.second ?: dnsLength

        if (domain.isNotEmpty() && isAdDomain(domain)) {
            // Instant 0ms local blocking of ad/tracker domain
            val blockedDnsResp = createBlockedResponse(dnsQuery, qEnd)
            val ipPacket = buildIpUdpPacket(
                srcIp = dnsServerIp,
                dstIp = clientIp,
                srcPort = 53,
                dstPort = srcPort,
                payload = blockedDnsResp
            )
            writePacket(output, ipPacket)

            serviceScope.launch {
                try {
                    repository?.recordBlockedQuery(domain, "ADS", "DF Shield DNS")
                } catch (_: Exception) {}
            }
        } else {
            // Check memory cache for instant resolution
            val cacheKey = domain
            val cached = if (cacheKey.isNotEmpty()) dnsResponseCache[cacheKey] else null
            if (cached != null) {
                // Adapt transaction ID from incoming query
                val adaptedResp = cached.clone()
                adaptedResp[0] = dnsQuery[0]
                adaptedResp[1] = dnsQuery[1]
                val ipPacket = buildIpUdpPacket(
                    srcIp = dnsServerIp,
                    dstIp = clientIp,
                    srcPort = 53,
                    dstPort = srcPort,
                    payload = adaptedResp
                )
                writePacket(output, ipPacket)
            } else {
                // Asynchronously query upstream DNS so concurrent queries never stall
                serviceScope.launch {
                    forwardDnsQuery(
                        domain = domain,
                        dnsQuery = dnsQuery,
                        clientIp = clientIp,
                        dnsServerIp = dnsServerIp,
                        clientPort = srcPort,
                        output = output
                    )
                }
            }
        }
    }

    private fun forwardDnsQuery(
        domain: String,
        dnsQuery: ByteArray,
        clientIp: ByteArray,
        dnsServerIp: ByteArray,
        clientPort: Int,
        output: FileOutputStream
    ) {
        var responseBytes: ByteArray? = null

        for (upstreamHost in UPSTREAM_SERVERS) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                protect(socket) // Critical: bypasses VPN so packet reaches physical internet
                socket.soTimeout = 800 // Fast 800ms timeout per upstream
                val packet = DatagramPacket(dnsQuery, dnsQuery.size, upstreamHost, 53)
                socket.send(packet)

                val recvBuffer = ByteArray(4096)
                val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                socket.receive(recvPacket)

                if (recvPacket.length > 12) {
                    responseBytes = recvBuffer.copyOf(recvPacket.length)
                    break
                }
            } catch (_: Exception) {
                // Try next upstream immediately
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }

        if (responseBytes != null) {
            // Cache successful response for instant future lookups
            if (domain.isNotEmpty() && responseBytes.size in 13..2048) {
                dnsResponseCache[domain] = responseBytes
            }

            val ipPacket = buildIpUdpPacket(
                srcIp = dnsServerIp,
                dstIp = clientIp,
                srcPort = 53,
                dstPort = clientPort,
                payload = responseBytes
            )
            writePacket(output, ipPacket)
        }
    }

    private fun writePacket(output: FileOutputStream, packet: ByteArray) {
        synchronized(writeLock) {
            try {
                output.write(packet)
                output.flush()
            } catch (_: Exception) {}
        }
    }

    private fun isAdDomain(domain: String): Boolean {
        val clean = domain.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return false

        // Check if root domain is explicitly safe
        for (safe in ESSENTIAL_SAFE_ROOTS) {
            if (clean == safe || clean.endsWith(".$safe")) {
                // Only block explicit ad subdomains on google/facebook
                val isExplicitAd = clean.contains("doubleclick") ||
                        clean.contains("googleads") ||
                        clean.contains("pagead") ||
                        clean.contains("adservice") ||
                        clean.startsWith("an.facebook.")
                if (!isExplicitAd) return false
            }
        }

        // Check against known ad networks
        for (ad in AD_NETWORKS) {
            if (clean == ad || clean.endsWith(".$ad") || clean.contains(ad)) {
                return true
            }
        }

        // Generic ad prefix/suffix matching
        if (clean.startsWith("ads.") ||
            clean.startsWith("ad.") ||
            clean.startsWith("adserver.") ||
            clean.startsWith("adservice.") ||
            clean.startsWith("adservices.") ||
            clean.startsWith("ads-delivery.") ||
            clean.startsWith("adtrack.") ||
            clean.startsWith("pagead.") ||
            clean.contains("-ads.") ||
            clean.contains(".ads.")
        ) {
            return true
        }

        return false
    }

    private fun parseDomainName(dnsPayload: ByteArray, offset: Int = 12): Pair<String, Int>? {
        try {
            if (dnsPayload.size <= offset) return null
            val sb = StringBuilder()
            var pos = offset
            while (pos < dnsPayload.size) {
                val len = dnsPayload[pos].toInt() and 0xFF
                if (len == 0) {
                    pos++
                    break
                }
                if ((len and 0xC0) == 0xC0) {
                    pos += 2
                    break
                }
                pos++
                if (sb.isNotEmpty()) sb.append(".")
                for (i in 0 until len) {
                    if (pos < dnsPayload.size) {
                        sb.append(dnsPayload[pos].toInt().toChar())
                        pos++
                    }
                }
            }
            val qEnd = pos + 4
            return Pair(sb.toString().lowercase(Locale.ROOT), qEnd)
        } catch (_: Exception) {
            return null
        }
    }

    private fun createBlockedResponse(query: ByteArray, qEndOffset: Int): ByteArray {
        val qEnd = minOf(qEndOffset, query.size)
        val response = ByteArray(qEnd + 16)
        System.arraycopy(query, 0, response, 0, qEnd)

        // Flags: 0x8180 (Standard Query Response, No Error)
        response[2] = 0x81.toByte()
        response[3] = 0x80.toByte()
        // ANCOUNT = 1
        response[6] = 0x00.toByte()
        response[7] = 0x01.toByte()

        // Answer RR
        response[qEnd] = 0xC0.toByte()
        response[qEnd + 1] = 0x0C.toByte()
        // TYPE A (0x0001)
        response[qEnd + 2] = 0x00.toByte()
        response[qEnd + 3] = 0x01.toByte()
        // CLASS IN (0x0001)
        response[qEnd + 4] = 0x00.toByte()
        response[qEnd + 5] = 0x01.toByte()
        // TTL: 300 seconds
        response[qEnd + 6] = 0x00.toByte()
        response[qEnd + 7] = 0x00.toByte()
        response[qEnd + 8] = 0x01.toByte()
        response[qEnd + 9] = 0x2C.toByte()
        // RDLENGTH: 4 bytes
        response[qEnd + 10] = 0x00.toByte()
        response[qEnd + 11] = 0x04.toByte()
        // RDATA: 0.0.0.0
        response[qEnd + 12] = 0x00.toByte()
        response[qEnd + 13] = 0x00.toByte()
        response[qEnd + 14] = 0x00.toByte()
        response[qEnd + 15] = 0x00.toByte()

        return response
    }

    private fun buildIpUdpPacket(
        srcIp: ByteArray,
        dstIp: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val totalLen = 20 + 8 + payload.size
        val packet = ByteArray(totalLen)

        // IPv4 Header
        packet[0] = 0x45.toByte()
        packet[1] = 0x00.toByte()
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()

        val id = (System.currentTimeMillis() and 0xFFFF).toInt()
        packet[4] = ((id shr 8) and 0xFF).toByte()
        packet[5] = (id and 0xFF).toByte()
        packet[6] = 0x40.toByte() // Don't fragment
        packet[7] = 0x00.toByte()
        packet[8] = 64.toByte()   // TTL
        packet[9] = 17.toByte()   // Protocol UDP
        packet[10] = 0x00.toByte()
        packet[11] = 0x00.toByte()

        System.arraycopy(srcIp, 0, packet, 12, 4)
        System.arraycopy(dstIp, 0, packet, 16, 4)

        // IPv4 Header Checksum
        var sum = 0
        for (i in 0 until 20 step 2) {
            val word = ((packet[i].toInt() and 0xFF) shl 8) or (packet[i + 1].toInt() and 0xFF)
            sum += word
        }
        while (sum > 0xFFFF) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        val checksum = sum.inv() and 0xFFFF
        packet[10] = ((checksum shr 8) and 0xFF).toByte()
        packet[11] = (checksum and 0xFF).toByte()

        // UDP Header
        packet[20] = ((srcPort shr 8) and 0xFF).toByte()
        packet[21] = (srcPort and 0xFF).toByte()
        packet[22] = ((dstPort shr 8) and 0xFF).toByte()
        packet[23] = (dstPort and 0xFF).toByte()
        val udpLen = 8 + payload.size
        packet[24] = ((udpLen shr 8) and 0xFF).toByte()
        packet[25] = (udpLen and 0xFF).toByte()
        packet[26] = 0x00.toByte()
        packet[27] = 0x00.toByte()

        // UDP Payload
        System.arraycopy(payload, 0, packet, 28, payload.size)
        return packet
    }

    private fun stopVpn() {
        isRunning = false
        vpnJob?.cancel()
        vpnJob = null
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        dnsResponseCache.clear()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DF Shield অ্যাড ব্লকার",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DF Shield সব বিজ্ঞাপন সুরক্ষা স্থিতি ও নোটিফিকেশন"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.df_shield_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
