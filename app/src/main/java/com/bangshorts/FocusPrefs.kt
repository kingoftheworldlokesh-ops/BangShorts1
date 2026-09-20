package com.bangshorts

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalTime

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
            ?: "youtube,instagram,facebook,browser"
        return raw.split(',').filter(String::isNotBlank).toSet()
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

    fun setNightModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
    }

    fun isMorningModeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MORNING_MODE, false)

    fun setMorningModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MORNING_MODE, enabled).apply()
    }

    fun isScheduleActive(context: Context, now: LocalTime = LocalTime.now()): Boolean {
        val nightActive = isNightModeEnabled(context) &&
            (now >= LocalTime.of(22, 0) || now < LocalTime.of(7, 0))
        val morningActive = isMorningModeEnabled(context) &&
            now >= LocalTime.of(6, 0) && now < LocalTime.of(9, 0)
        return nightActive || morningActive
    }

    fun getBlockCount(context: Context): Int = prefs(context).getInt(KEY_BLOCK_COUNT, 0)

    fun incrementBlockCount(context: Context) {
        val next = getBlockCount(context) + 1
        prefs(context).edit().putInt(KEY_BLOCK_COUNT, next).apply()
    }

    fun getStreakDays(context: Context): Int = prefs(context).getInt(KEY_STREAK_DAYS, 0)

    fun setStreakDays(context: Context, days: Int) {
        prefs(context).edit().putInt(KEY_STREAK_DAYS, days.coerceAtLeast(0)).apply()
    }

    fun getSessionMinutes(context: Context): Int = prefs(context).getInt(KEY_SESSION_MINUTES, 0)

    fun setSessionMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_SESSION_MINUTES, minutes.coerceAtLeast(0)).apply()
    }
}
