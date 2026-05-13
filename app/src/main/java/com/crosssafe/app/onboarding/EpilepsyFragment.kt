package com.crosssafe.app.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.crosssafe.app.OnboardingActivity
import com.crosssafe.app.R

class EpilepsyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_epilepsy, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnContinue = view.findViewById<Button>(R.id.btnContinue)
        btnContinue.isEnabled = false
        btnContinue.text = "Please read (2s)..."

        Handler(Looper.getMainLooper()).postDelayed({
            btnContinue.isEnabled = true
            btnContinue.text = "I understand — Continue"
            (activity as? OnboardingActivity)?.enableNextButton()
        }, 2000L)

        btnContinue.setOnClickListener {
            (activity as? OnboardingActivity)?.goToNextCard()
        }
    }
}
