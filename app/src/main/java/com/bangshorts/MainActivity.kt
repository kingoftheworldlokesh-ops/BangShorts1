package com.bangshorts

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.bangshorts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.enableServiceButton.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isAccessibilityEnabled()) {
                openAccessibilitySettings()
            }
            refreshStatus()
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val enabled = isAccessibilityEnabled()
        binding.statusSwitch.isChecked = enabled
        binding.statusText.text = if (enabled) {
            getString(R.string.service_enabled)
        } else {
            getString(R.string.service_disabled)
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val accessibilityManager = getSystemService(AccessibilityManager::class.java)
        val enabledServices = accessibilityManager?.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
        ) ?: return false

        return enabledServices.any { service ->
            service.resolveInfo.serviceInfo.packageName == packageName &&
                service.resolveInfo.serviceInfo.name == ShortsBlockAccessibilityService::class.java.name
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
