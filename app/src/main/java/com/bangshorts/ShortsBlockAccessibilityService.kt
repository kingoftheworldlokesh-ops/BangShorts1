package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsBlockAccessibilityService : AccessibilityService() {

    private val protectedPackages = setOf(
        "com.google.android.youtube",
        "com.android.chrome",
        "com.google.android.apps.youtube.music",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.lite"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = protectedPackages.toTypedArray()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!FocusPrefs.isFocusModeEnabled(this)) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in protectedPackages) return

        val rootNode = rootInActiveWindow ?: return
        val text = collectNodeText(rootNode)
        val blockedTerms = listOf("shorts", "reels", "watch", "video")

        if (blockedTerms.any { term -> text.contains(term, ignoreCase = true) }) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() = Unit

    private fun collectNodeText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        val builder = StringBuilder()
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            builder.append(text).append(" ")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                builder.append(collectNodeText(child))
                child.recycle()
            }
        }

        return builder.toString()
    }
}
