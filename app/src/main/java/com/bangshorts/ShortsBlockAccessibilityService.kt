package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Blocks short-form destinations without treating an entire app as blocked.
 * Accessibility text varies by app version, so the rules intentionally require
 * player/feed-specific phrases instead of matching broad words such as "video".
 */
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
            notificationTimeout = 250
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val preferenceKey = packageToPreference[packageName] ?: return
        if (preferenceKey !in FocusPrefs.getProtectedApps(this)) return
        if (!FocusPrefs.isFocusModeEnabled(this) && !FocusPrefs.isScheduleActive(this)) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastBlockAt < BLOCK_COOLDOWN_MS) return

        val root = rootInActiveWindow ?: return
        val screenText = collectNodeText(root)
        if (!isShortFormPlayer(screenText, packageName)) return

        lastBlockAt = now
        FocusPrefs.incrementBlockCount(this)
        FocusPrefs.setStreakDays(this, (FocusPrefs.getStreakDays(this) + 1).coerceAtMost(365))
        FocusPrefs.setSessionMinutes(this, FocusPrefs.getSessionMinutes(this) + 1)

        // Back from the short-form player returns to the app's previous screen.
        // We never close the host app directly.
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() = Unit

    private fun isShortFormPlayer(text: String, packageName: String): Boolean {
        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim()

        return when (packageName) {
            "com.google.android.youtube", "com.google.android.apps.youtube.music" -> {
                // Do not match the standalone navigation label "Shorts". The
                // player/feed indicators below are much less likely to appear
                // on the normal YouTube home screen.
                listOf(
                    "/shorts/",
                    "youtube shorts player",
                    "shorts player",
                    "shorts feed",
                    "shorts video"
                ).any(normalized::contains)
            }

            "com.instagram.android" -> listOf(
                "reels player",
                "reels feed",
                "reel video",
                "instagram reels"
            ).any(normalized::contains)

            "com.facebook.katana", "com.facebook.lite" -> listOf(
                "reels player",
                "reels feed",
                "facebook reels",
                "watch reels"
            ).any(normalized::contains)

            "com.android.chrome" -> listOf(
                "youtube.com/shorts/",
                "youtube.com/shorts",
                "instagram.com/reels/",
                "facebook.com/reel/"
            ).any(normalized::contains)

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
        const val BLOCK_COOLDOWN_MS = 2_000L
    }
}
