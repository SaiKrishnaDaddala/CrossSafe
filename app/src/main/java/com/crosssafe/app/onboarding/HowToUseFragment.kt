package com.crosssafe.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crosssafe.app.R

class HowToUseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_how_to_use, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateStepsIn(view)
    }

    private fun animateStepsIn(view: View) {
        val stepIds = listOf(R.id.step1, R.id.step2, R.id.step3, R.id.step4)
        stepIds.forEachIndexed { index, id ->
            view.findViewById<View>(id)?.let { stepView ->
                stepView.alpha = 0f
                stepView.translationY = 30f
                stepView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 120L)
                    .setDuration(300L)
                    .start()
            }
        }
    }
}
