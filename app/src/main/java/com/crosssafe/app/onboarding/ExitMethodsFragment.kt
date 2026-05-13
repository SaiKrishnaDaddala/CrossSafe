package com.crosssafe.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crosssafe.app.R

class ExitMethodsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_exit_methods, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateRowsIn(view)
    }

    private fun animateRowsIn(view: View) {
        val rowIds = listOf(R.id.exitRow1, R.id.exitRow2, R.id.exitRow3, R.id.exitRow4, R.id.exitRow5, R.id.exitRow6)
        rowIds.forEachIndexed { index, id ->
            view.findViewById<View>(id)?.let { rowView ->
                rowView.alpha = 0f
                rowView.translationX = -40f
                rowView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(index * 100L)
                    .setDuration(300L)
                    .start()
            }
        }
    }
}
