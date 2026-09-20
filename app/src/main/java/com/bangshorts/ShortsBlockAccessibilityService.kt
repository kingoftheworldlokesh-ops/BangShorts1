package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Leaves normal YouTube navigation and long-form videos alone. A block is
 * triggered only when the active screen looks like a Shorts/Reels player:
 * the screen must contain a short-form label plus at least two player-control
 * signals. This avoids treating the YouTube home tab's "Shorts" label as a
 * Shorts video.
 */
class ShortsBlockAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
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
                AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = packageToPreference.keys.toTypedArray()
            notificationTimeout = 200
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val preferenceKey = packageToPreference[packageName] ?: return
        if (preferenceKey !in FocusPrefs.getProtectedApps(this)) return
        if (!FocusPrefs.isFocusModeEnabled(this) && !FocusPrefs.isScheduleActive(this)) return

        // Shorts UI often appears a moment after the window-change event.
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ inspectCurrentScreen(packageName, preferenceKey) }, SCAN_DELAY_MS)
    }

    private fun inspectCurrentScreen(packageName: String, preferenceKey: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBlockAt < BLOCK_COOLDOWN_MS) return

        val root = rootInActiveWindow ?: return
        val screenText = collectNodeText(root)
        if (!isShortFormPlayer(screenText, packageName)) return

        lastBlockAt = now
        FocusPrefs.incrementBlockCount(this)
        FocusPrefs.setStreakDays(this, (FocusPrefs.getStreakDays(this) + 1).coerceAtMost(365))
        FocusPrefs.setSessionMinutes(this, FocusPrefs.getSessionMinutes(this) + 1)

        // Back leaves the Shorts/Reels player; it does not close the host app.
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun isShortFormPlayer(text: String, packageName: String): Boolean {
        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim()
        val controls = listOf(
            "like", "dislike", "comments", "comment", "share", "subscribe",
            "remix", "more options", "save", "follow"
        )
        val controlCount = controls.count { normalized.contains(it) }

        return when (packageName) {
            "com.google.android.youtube", "com.google.android.apps.youtube.music" -> {
                val shortFormLabel = listOf("shorts", "short video", "shorts player")
                    .any(normalized::contains)
                val nonShortsNavigationOnly = normalized.contains("home") &&
                    normalized.contains("subscriptions") && controlCount < 2
                shortFormLabel && controlCount >= 2 && !nonShortsNavigationOnly
            }

            "com.instagram.android" -> {
                normalized.contains("reels") && controlCount >= 2
            }

            "com.facebook.katana", "com.facebook.lite" -> {
                normalized.contains("reels") && controlCount >= 2
            }

            "com.android.chrome" -> {
                listOf(
                    "youtube.com/shorts/", "youtube.com/shorts",
                    "instagram.com/reels/", "facebook.com/reel/"
                ).any(normalized::contains)
            }

            else -> false
        }
    }

    private fun collectNodeText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        return buildString {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let {
                append(it).append(' ')
            }
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let {
                append(it).append(' ')
            }
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let { child ->
                    append(collectNodeText(child))
                    child.recycle()
                }
            }
        }
    }

    private companion object {
        const val SCAN_DELAY_MS = 350L
        const val BLOCK_COOLDOWN_MS = 2_000L
    }
}
