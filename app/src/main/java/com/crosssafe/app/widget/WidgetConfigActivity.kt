package com.crosssafe.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crosssafe.app.R
import com.crosssafe.app.data.PresetRepository
import com.crosssafe.app.databinding.ActivityWidgetConfigBinding

class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedPresetId = "police"
    private var selectedWidgetType = "quick_go"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configure widget"

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        val repo = PresetRepository(this)
        val presets = repo.getAllPresets()

        binding.rgWidgetType.setOnCheckedChangeListener { _, checkedId ->
            selectedWidgetType = when (checkedId) {
                R.id.rbPresetStrip -> "preset_strip"
                R.id.rbFullWidget -> "full_widget"
                else -> "quick_go"
            }
        }

        binding.btnSaveWidget.setOnClickListener { saveAndFinish() }
    }

    private fun saveAndFinish() {
        val prefs = getSharedPreferences("widget_$appWidgetId", MODE_PRIVATE)
        prefs.edit()
            .putString("preset_id", selectedPresetId)
            .putString("widget_type", selectedWidgetType)
            .apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        CrossSafeWidgetProvider().onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
