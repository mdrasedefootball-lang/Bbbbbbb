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
import java.util.Locale

class DFShieldVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val writeLock = Any()
    private var repository: ShieldRepository? = null

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val CHANNEL_ID = "df_shield_protection_channel"
        const val NOTIFICATION_ID = 1001

        var isRunning = false
            private set

        // High frequency ad & tracking domains blocked instantly locally
        private val AD_DOMAINS = setOf(
            "doubleclick.net",
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "googleadservices.com",
            "adnxs.com",
            "criteo.com",
            "taboola.com",
            "outbrain.com",
            "scorecardresearch.com",
            "quantserve.com",
            "moatads.com",
            "rubiconproject.com",
            "pubmatic.com",
            "openx.net",
            "casalemedia.com",
            "adcolony.com",
            "vungle.com",
            "unityads.unity3d.com",
            "applovin.com",
            "ironsrc.com",
            "chartboost.com",
            "inmobi.com",
            "popads.net",
            "propellerads.com",
            "bidswitch.net",
            "smartadserver.com",
            "adroll.com",
            "yieldmo.com",
            "advertising.com",
            "appsflyer.com",
            "adjust.com",
            "branch.io"
        )

        // Upstream DNS servers for real, unblocked, high-speed resolution
        private val UPSTREAM_DNS = listOf(
            "94.140.14.14", // AdGuard Primary (DNS level ad filtering)
            "94.140.15.15", // AdGuard Secondary
            "1.1.1.1",      // Cloudflare Fast Fallback (Guarantees internet is never lost)
            "8.8.8.8"       // Google Fast Fallback
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
            "অ্যাড ব্লকার সুরক্ষা চালু",
            "ইন্টারনেট সম্পূর্ণ গতিতে সচল আছে এবং শুধু বিজ্ঞাপন ব্লক করা হচ্ছে।"
        )
        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession("DF Shield AdBlock")
                .addAddress("10.240.0.1", 32)
                .addDnsServer("10.240.0.1")
                .addDnsServer("94.140.14.14")
                // Only route the DNS proxy addresses through the VPN!
                // All other internet traffic (web, video, social, apps) goes directly
                // through Wi-Fi or Mobile data at 100% full hardware speed!
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

            // Launch active DNS proxy loop
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
                    // Closed cleanly on shutdown
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
        // Validate IPv4
        val versionAndIhl = buffer[0].toInt() and 0xFF
        val version = versionAndIhl shr 4
        if (version != 4) return
        val ihl = (versionAndIhl and 0x0F) * 4
        if (ihl < 20 || length < ihl + 8) return

        // Validate UDP (Protocol 17)
        val protocol = buffer[9].toInt() and 0xFF
        if (protocol != 17) return

        val srcPort = ((buffer[ihl].toInt() and 0xFF) shl 8) or (buffer[ihl + 1].toInt() and 0xFF)
        val dstPort = ((buffer[ihl + 2].toInt() and 0xFF) shl 8) or (buffer[ihl + 3].toInt() and 0xFF)
        val udpLength = ((buffer[ihl + 4].toInt() and 0xFF) shl 8) or (buffer[ihl + 5].toInt() and 0xFF)

        // Only process DNS queries (destination port 53)
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

        // Parse requested domain name
        val parsed = parseDomainName(dnsQuery)
        val domain = parsed?.first ?: ""
        val qEnd = parsed?.second ?: dnsLength

        if (domain.isNotEmpty() && isAdDomain(domain)) {
            // Block ad domain immediately (0ms) by returning 0.0.0.0
            val blockedDnsResp = createBlockedResponse(dnsQuery, qEnd)
            val ipPacket = buildIpUdpPacket(
                srcIp = dnsServerIp,
                dstIp = clientIp,
                srcPort = 53,
                dstPort = srcPort,
                payload = blockedDnsResp
            )
            synchronized(writeLock) {
                try {
                    output.write(ipPacket)
                    output.flush()
                } catch (_: Exception) {}
            }
            // Record blocked log in repository
            serviceScope.launch {
                try {
                    repository?.recordBlockedQuery(domain, "ADS", "DNS Shield")
                } catch (_: Exception) {}
            }
        } else {
            // Forward legitimate domain to upstream DNS via protected socket
            // Normal internet traffic (Google, YouTube, Facebook, WhatsApp, news, etc.)
            // resolves instantly at full speed!
            serviceScope.launch {
                forwardDnsQuery(
                    dnsQuery = dnsQuery,
                    clientIp = clientIp,
                    dnsServerIp = dnsServerIp,
                    clientPort = srcPort,
                    output = output
                )
            }
        }
    }

    private fun forwardDnsQuery(
        dnsQuery: ByteArray,
        clientIp: ByteArray,
        dnsServerIp: ByteArray,
        clientPort: Int,
        output: FileOutputStream
    ) {
        var responseBytes: ByteArray? = null

        for (upstreamHost in UPSTREAM_DNS) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                // Protect socket so its outbound traffic bypasses the VPN tunnel
                protect(socket)
                socket.soTimeout = 1200 // 1.2s timeout per upstream
                val upstreamAddress = InetAddress.getByName(upstreamHost)
                val packet = DatagramPacket(dnsQuery, dnsQuery.size, upstreamAddress, 53)
                socket.send(packet)

                val recvBuffer = ByteArray(4096)
                val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                socket.receive(recvPacket)

                if (recvPacket.length > 12) {
                    responseBytes = recvBuffer.copyOf(recvPacket.length)
                    break
                }
            } catch (_: Exception) {
                // Try next fast DNS server in case of mobile network glitch
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }

        if (responseBytes != null) {
            val ipPacket = buildIpUdpPacket(
                srcIp = dnsServerIp,
                dstIp = clientIp,
                srcPort = 53,
                dstPort = clientPort,
                payload = responseBytes
            )
            synchronized(writeLock) {
                try {
                    output.write(ipPacket)
                    output.flush()
                } catch (_: Exception) {}
            }
        }
    }

    private fun isAdDomain(domain: String): Boolean {
        val clean = domain.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return false

        for (ad in AD_DOMAINS) {
            if (clean == ad || clean.endsWith(".$ad")) {
                return true
            }
        }
        if (clean.startsWith("ads.") || clean.startsWith("adserver.") || clean.startsWith("adservice.")) {
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
            val qEnd = pos + 4 // skip QTYPE (2) + QCLASS (2)
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
        // Name pointer to QNAME (0xC00C)
        response[qEnd] = 0xC0.toByte()
        response[qEnd + 1] = 0x0C.toByte()
        // TYPE A (0x0001)
        response[qEnd + 2] = 0x00.toByte()
        response[qEnd + 3] = 0x01.toByte()
        // CLASS IN (0x0001)
        response[qEnd + 4] = 0x00.toByte()
        response[qEnd + 5] = 0x01.toByte()
        // TTL: 300s (0x0000012C)
        response[qEnd + 6] = 0x00.toByte()
        response[qEnd + 7] = 0x00.toByte()
        response[qEnd + 8] = 0x01.toByte()
        response[qEnd + 9] = 0x2C.toByte()
        // RDLENGTH: 4
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

        // Version 4, IHL 5 (20 bytes)
        packet[0] = 0x45.toByte()
        packet[1] = 0x00.toByte()
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()

        val id = (System.currentTimeMillis() and 0xFFFF).toInt()
        packet[4] = ((id shr 8) and 0xFF).toByte()
        packet[5] = (id and 0xFF).toByte()
        // Flags (Don't Fragment = 0x4000)
        packet[6] = 0x40.toByte()
        packet[7] = 0x00.toByte()
        packet[8] = 64.toByte() // TTL
        packet[9] = 17.toByte() // Protocol = UDP
        packet[10] = 0x00.toByte() // Checksum placeholder
        packet[11] = 0x00.toByte()

        System.arraycopy(srcIp, 0, packet, 12, 4)
        System.arraycopy(dstIp, 0, packet, 16, 4)

        // Calculate IPv4 Header Checksum
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
        // UDP Checksum = 0x0000 (valid and standard in IPv4)
        packet[26] = 0x00.toByte()
        packet[27] = 0x00.toByte()

        // Payload
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
                description = "DF Shield সুরক্ষা স্থিতি ও নোটিফিকেশন"
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
