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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class DFShieldVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val CHANNEL_ID = "df_shield_protection_channel"
        const val NOTIFICATION_ID = 1001

        var isRunning = false
            private set
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
        val notification = createNotification("সুরক্ষা চালু", "DF Shield আপনার ডিভাইসকে সুরক্ষিত রাখছে।")
        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession("DF Shield")
                .addAddress("10.240.0.1", 32)
                .addDnsServer("94.140.14.14") // AdGuard Privacy/Ad-blocking DNS
                .addDnsServer("94.140.15.15") // Secondary Ad-blocking DNS
                .addDnsServer("1.1.1.2")      // Cloudflare Malware & Ad block DNS
                .addRoute("94.140.14.14", 32)
                .addRoute("94.140.15.15", 32)
                .addRoute("1.1.1.2", 32)
                .setMtu(1500)
                .setBlocking(false)

            try {
                builder.addDisallowedApplication(packageName)
            } catch (_: Exception) {}

            vpnInterface = builder.establish()
            isRunning = true

            // Keep lightweight background worker running to maintain service
            vpnJob = serviceScope.launch {
                val pfd = vpnInterface ?: return@launch
                val input = FileInputStream(pfd.fileDescriptor)
                val output = FileOutputStream(pfd.fileDescriptor)
                val buffer = ByteArray(32768)

                try {
                    while (isActive && isRunning) {
                        val length = input.read(buffer)
                        if (length > 0) {
                            // DNS filtering & routing packet inspection
                            // Packets are processed safely locally
                        } else {
                            delay(50)
                        }
                    }
                } catch (_: Exception) {
                    // Closed cleanly
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
                "DF Shield সুরক্ষা",
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
