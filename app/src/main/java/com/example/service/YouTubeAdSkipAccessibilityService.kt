package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.repository.ShieldRepository

class YouTubeAdSkipAccessibilityService : AccessibilityService() {

    private var repository: ShieldRepository? = null
    private var lastSkipTime = 0L

    companion object {
        var isServiceRunning: Boolean = false
            private set

        private val SKIP_VIEW_IDS = listOf(
            "com.google.android.youtube:id/skip_ad_button",
            "com.google.android.youtube:id/modern_skip_ad_button",
            "com.google.android.youtube:id/skip_ad_button_text",
            "com.google.android.youtube:id/ad_skip_button",
            "com.google.android.youtube:id/action_button"
        )

        private val SKIP_TEXTS = listOf(
            "Skip Ad",
            "Skip Ads",
            "Skip ad",
            "Skip ads",
            "Skip",
            "বিজ্ঞাপন এড়িয়ে যান",
            "এড়িয়ে যান",
            "বিজ্ঞাপন বাদ দিন"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        repository = ShieldRepository(applicationContext)

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 80
            packageNames = arrayOf("com.google.android.youtube", "com.google.android.youtube.tv")
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return

        try {
            findAndClickSkipButton(rootNode)
        } catch (_: Exception) {
        } finally {
            try {
                rootNode.recycle()
            } catch (_: Exception) {}
        }
    }

    private fun findAndClickSkipButton(root: AccessibilityNodeInfo): Boolean {
        val now = SystemClock.uptimeMillis()
        if (now - lastSkipTime < 600) return false

        // 1. Try finding by known view resource IDs
        for (id in SKIP_VIEW_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (clickNodeOrParent(node)) {
                        lastSkipTime = now
                        repository?.incrementYouTubeBlocked()
                        recycleNodes(nodes)
                        return true
                    }
                }
                recycleNodes(nodes)
            }
        }

        // 2. Try finding by known localized skip texts
        for (skipText in SKIP_TEXTS) {
            val nodes = root.findAccessibilityNodeInfosByText(skipText)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (clickNodeOrParent(node)) {
                        lastSkipTime = now
                        repository?.incrementYouTubeBlocked()
                        recycleNodes(nodes)
                        return true
                    }
                }
                recycleNodes(nodes)
            }
        }

        return false
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable) {
                val clicked = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (clicked) return true
            }
            current = current.parent
            depth++
        }
        return false
    }

    private fun recycleNodes(nodes: List<AccessibilityNodeInfo>) {
        for (n in nodes) {
            try {
                n.recycle()
            } catch (_: Exception) {}
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
