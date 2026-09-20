package com.bangshorts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangshorts.databinding.ActivityStatsBinding

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderStats()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun renderStats() {
        val blockedCount = FocusPrefs.getBlockCount(this)
        val reduction = minOf(99, maxOf(12, (blockedCount * 7) / 3))
        val minutes = FocusPrefs.getSessionMinutes(this)
        val hours = minutes / 60
        val mins = minutes % 60

        binding.blockedStatsText.text = blockedCount.toString()
        binding.reductionText.text = "${reduction}%"
        binding.sessionTimeText.text = if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }
}
