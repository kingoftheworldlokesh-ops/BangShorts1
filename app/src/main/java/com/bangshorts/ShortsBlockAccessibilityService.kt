package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsBlockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = arrayOf(
                "com.google.android.youtube",
                "com.android.chrome",
                "com.google.android.apps.youtube.music"
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName !in listOf(
                "com.google.android.youtube",
                "com.android.chrome",
                "com.google.android.apps.youtube.music"
            )) {
            return
        }

        val rootNode = rootInActiveWindow ?: return
        val text = collectNodeText(rootNode)

        if (text.contains("shorts", ignoreCase = true)) {
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
