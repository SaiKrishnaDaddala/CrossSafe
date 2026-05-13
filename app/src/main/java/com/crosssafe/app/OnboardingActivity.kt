package com.crosssafe.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.crosssafe.app.databinding.ActivityOnboardingBinding
import com.crosssafe.app.model.PrefKeys
import com.crosssafe.app.onboarding.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val prefs by lazy { getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        setupDots(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                binding.btnSkip.visibility = if (position == 0) View.GONE else View.VISIBLE
                binding.btnNext.text = if (position == 3) "Get Started" else "Next →"
            }
        })

        binding.btnSkip.setOnClickListener { completeOnboarding() }

        binding.btnNext.visibility = View.INVISIBLE

        binding.btnSkip.visibility = View.GONE
    }

    fun enableNextButton() {
        binding.btnNext.visibility = View.VISIBLE
        binding.btnNext.isEnabled = true
        binding.btnNext.setOnClickListener { goToNextCard() }
    }

    fun goToNextCard() {
        val current = binding.viewPager.currentItem
        if (current < 3) {
            binding.viewPager.currentItem = current + 1
        } else {
            completeOnboarding()
        }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean(PrefKeys.ONBOARDING_COMPLETE, true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupDots(currentPage: Int) {
        binding.dotsContainer.removeAllViews()
        for (i in 0..3) {
            val dot = TextView(this).apply {
                text = if (i == currentPage) "●" else "○"
                textSize = 14f
                setTextColor(if (i == currentPage) 0xFFEF4444.toInt() else 0xFF9CA3AF.toInt())
                setPadding(6, 0, 6, 0)
            }
            binding.dotsContainer.addView(dot)
        }
    }
}
