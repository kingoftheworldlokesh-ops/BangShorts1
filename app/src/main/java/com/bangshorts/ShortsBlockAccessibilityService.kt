package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsBlockAccessibilityService : AccessibilityService() {

    private var lastBlockAt = 0L

    private val packageToPreference = mapOf(
        "com.google.android.youtube" to "youtube",
        "com.google.android.apps.youtube.music" to "youtube",
        "com.instagram.android" to "instagram",
        "com.facebook.katana" to "facebook",
        "com.facebook.lite" to "facebook",
        "com.android.chrome" to "browser"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = packageToPreference.keys.toTypedArray()
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val preferenceKey = packageToPreference[event.packageName?.toString()] ?: return
        val protectedApps = FocusPrefs.getProtectedApps(this)
        if (preferenceKey !in protectedApps) return
        if (!FocusPrefs.isFocusModeEnabled(this) && !FocusPrefs.isScheduleActive(this)) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastBlockAt < 1_500L) return

        val root = rootInActiveWindow ?: return
        val screenText = collectNodeText(root)
        if (!containsBlockedContent(screenText, preferenceKey)) return

        lastBlockAt = now
        FocusPrefs.incrementBlockCount(this)
        FocusPrefs.setStreakDays(this, (FocusPrefs.getStreakDays(this) + 1).coerceAtMost(365))
        FocusPrefs.setSessionMinutes(this, FocusPrefs.getSessionMinutes(this) + 1)
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() = Unit

    private fun containsBlockedContent(text: String, app: String): Boolean {
        val normalized = text.lowercase()
        val terms = when (app) {
            "youtube" -> listOf("shorts", "short videos")
            "instagram" -> listOf("reels", "reel")
            "facebook" -> listOf("reels", "watch", "short videos")
            else -> listOf("youtube shorts", "instagram reels", "/shorts/")
        }
        return terms.any(normalized::contains)
    }

    private fun collectNodeText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val text = buildString {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let { append(it).append(' ') }
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { append(it).append(' ') }
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let { child ->
                    append(collectNodeText(child))
                    child.recycle()
                }
            }
        }
        return text
    }
}
