package com.bangshorts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangshorts.databinding.ActivityScheduleBinding

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nightModeSwitch.isChecked = FocusPrefs.isNightModeEnabled(this)
        binding.morningModeSwitch.isChecked = FocusPrefs.isMorningModeEnabled(this)
        binding.scheduleSummary.text = getString(R.string.schedule_summary)

        binding.saveScheduleButton.setOnClickListener {
            FocusPrefs.setNightModeEnabled(this, binding.nightModeSwitch.isChecked)
            FocusPrefs.setMorningModeEnabled(this, binding.morningModeSwitch.isChecked)
            finish()
        }
    }
}
