package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ShortsBlockAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit
}
