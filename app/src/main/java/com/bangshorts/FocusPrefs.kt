package com.bangshorts

import android.content.Context
import android.content.SharedPreferences

object FocusPrefs {
    private const val PREFS_NAME = "bangshorts_prefs"
    private const val KEY_PROTECTED_APPS = "protected_apps"
    private const val KEY_FOCUS_MODE = "focus_mode"
    private const val KEY_NIGHT_MODE = "night_mode"
    private const val KEY_MORNING_MODE = "morning_mode"
    private const val KEY_BLOCK_COUNT = "block_count"
    private const val KEY_STREAK_DAYS = "streak_days"
    private const val KEY_SESSION_MINUTES = "session_minutes"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProtectedApps(context: Context): Set<String> {
        val raw = prefs(context).getString(KEY_PROTECTED_APPS, null)
            ?: listOf("youtube", "instagram", "facebook", "browser").joinToString(",")
        return raw.split(',').filter { it.isNotBlank() }.toSet()
    }

    fun setProtectedApps(context: Context, apps: Set<String>) {
        prefs(context).edit().putString(KEY_PROTECTED_APPS, apps.joinToString(",")).apply()
    }

    fun isFocusModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FOCUS_MODE, false)

    fun setFocusModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
    }

    fun isNightModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NIGHT_MODE, false)

    fun isMorningModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MORNING_MODE, false)

    fun setNightModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
    }

    fun setMorningModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MORNING_MODE, enabled).apply()
    }

    fun getBlockCount(context: Context): Int =
        prefs(context).getInt(KEY_BLOCK_COUNT, 0)

    fun incrementBlockCount(context: Context) {
        val current = getBlockCount(context)
        prefs(context).edit().putInt(KEY_BLOCK_COUNT, current + 1).apply()
    }

    fun getStreakDays(context: Context): Int =
        prefs(context).getInt(KEY_STREAK_DAYS, 7)

    fun setStreakDays(context: Context, days: Int) {
        prefs(context).edit().putInt(KEY_STREAK_DAYS, days).apply()
    }

    fun getSessionMinutes(context: Context): Int =
        prefs(context).getInt(KEY_SESSION_MINUTES, 134)

    fun setSessionMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_SESSION_MINUTES, minutes).apply()
    }
}
