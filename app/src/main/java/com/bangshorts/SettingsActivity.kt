package com.bangshorts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangshorts.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedApps = FocusPrefs.getProtectedApps(this)
        binding.youtubeCheckBox.isChecked = selectedApps.contains("youtube")
        binding.instagramCheckBox.isChecked = selectedApps.contains("instagram")
        binding.facebookCheckBox.isChecked = selectedApps.contains("facebook")
        binding.browserCheckBox.isChecked = selectedApps.contains("browser")

        binding.saveButton.setOnClickListener {
            val apps = mutableSetOf<String>()
            if (binding.youtubeCheckBox.isChecked) apps += "youtube"
            if (binding.instagramCheckBox.isChecked) apps += "instagram"
            if (binding.facebookCheckBox.isChecked) apps += "facebook"
            if (binding.browserCheckBox.isChecked) apps += "browser"

            FocusPrefs.setProtectedApps(this, apps)
            finish()
        }
    }
}
