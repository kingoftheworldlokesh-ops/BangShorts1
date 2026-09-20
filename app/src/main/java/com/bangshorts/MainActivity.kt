package com.bangshorts

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import androidx.appcompat.app.AppCompatActivity
import com.bangshorts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.blockedCountText.text = FocusPrefs.getBlockCount(this).toString()
        binding.streakText.text = "${FocusPrefs.getStreakDays(this)} days"

        binding.openSettingsButton.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isAccessibilityEnabled()) {
                openAccessibilitySettings()
            }
            refreshStatus()
        }

        binding.focusModeButton.setOnClickListener {
            val enabled = !FocusPrefs.isFocusModeEnabled(this)
            FocusPrefs.setFocusModeEnabled(this, enabled)
            binding.focusModeButton.text = if (enabled) {
                getString(R.string.focus_mode_running)
            } else {
                getString(R.string.start_focus_mode)
            }
        }

        binding.viewStatsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.scheduleButton.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        binding.focusModeButton.text = if (FocusPrefs.isFocusModeEnabled(this)) {
            getString(R.string.focus_mode_running)
        } else {
            getString(R.string.start_focus_mode)
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        binding.blockedCountText.text = FocusPrefs.getBlockCount(this).toString()
        binding.streakText.text = "${FocusPrefs.getStreakDays(this)} days"
        refreshStatus()
    }

    private fun refreshStatus() {
        val enabled = isAccessibilityEnabled()
        binding.serviceSwitch.isChecked = enabled

        if (enabled) {
            binding.serviceStatus.text = getString(R.string.protection_active)
            binding.serviceStatus.setTextColor(getColor(R.color.success))
        } else {
            binding.serviceStatus.text = getString(R.string.protection_disabled)
            binding.serviceStatus.setTextColor(getColor(R.color.warning))
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val accessibilityManager = getSystemService(AccessibilityManager::class.java)
        val enabledServices = accessibilityManager?.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        ) ?: return false

        return enabledServices.any { service ->
            service.resolveInfo.serviceInfo.packageName == packageName &&
                service.resolveInfo.serviceInfo.name == ShortsBlockAccessibilityService::class.java.name
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
