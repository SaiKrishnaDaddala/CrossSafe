package com.crosssafe.app.onboarding

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.crosssafe.app.OnboardingActivity
import com.crosssafe.app.R
import com.crosssafe.app.widget.CrossSafeWidgetProvider

class WidgetTipFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_widget_tip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnAddWidget).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val appWidgetManager = AppWidgetManager.getInstance(requireContext())
                val provider = ComponentName(requireContext(), CrossSafeWidgetProvider::class.java)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    appWidgetManager.requestPinAppWidget(provider, null, null)
                }
            }
        }

        view.findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            (activity as? OnboardingActivity)?.completeOnboarding()
        }
    }
}
