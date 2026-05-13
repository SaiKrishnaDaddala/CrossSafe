package com.crosssafe.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.crosssafe.app.FlashActivity
import com.crosssafe.app.R
import com.crosssafe.app.data.PresetRepository

class CrossSafeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
        val presetId = prefs.getString("preset_id", "police") ?: "police"
        val widgetType = prefs.getString("widget_type", "quick_go") ?: "quick_go"

        val views = when (widgetType) {
            "preset_strip" -> buildPresetStripWidget(context, appWidgetId, presetId)
            "full_widget" -> buildFullWidget(context, appWidgetId, presetId)
            else -> buildQuickGoWidget(context, appWidgetId, presetId)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun buildQuickGoWidget(context: Context, appWidgetId: Int, presetId: String): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_go)
        val intent = buildFlashIntent(context, presetId)
        val pending = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetGoCircle, pending)
        return views
    }

    private fun buildPresetStripWidget(context: Context, appWidgetId: Int, presetId: String): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_preset_strip)
        val repo = PresetRepository(context)
        val preset = repo.getPresetById(presetId)
        preset?.let { views.setTextViewText(R.id.mainPresetName, it.name) }

        val mainIntent = buildFlashIntent(context, presetId)
        val mainPending = PendingIntent.getActivity(
            context, appWidgetId, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnGoMain, mainPending)

        val pinned = repo.getPinnedPresets()
        if (pinned.size > 1) {
            views.setTextViewText(R.id.preset1Name, pinned[1].name)
            val i1 = buildFlashIntent(context, pinned[1].id)
            val p1 = PendingIntent.getActivity(context, appWidgetId + 1, i1,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btnPreset1, p1)
        }
        if (pinned.size > 2) {
            views.setTextViewText(R.id.preset2Name, pinned[2].name)
            val i2 = buildFlashIntent(context, pinned[2].id)
            val p2 = PendingIntent.getActivity(context, appWidgetId + 2, i2,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btnPreset2, p2)
        }
        return views
    }

    private fun buildFullWidget(context: Context, appWidgetId: Int, presetId: String): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_full)
        val repo = PresetRepository(context)
        val preset = repo.getPresetById(presetId)
        preset?.let { views.setTextViewText(R.id.activePresetName, it.name) }

        val mainIntent = buildFlashIntent(context, presetId)
        val mainPending = PendingIntent.getActivity(context, appWidgetId, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.btnGoFull, mainPending)

        val pinned = repo.getPinnedPresets()
        val chipIds = listOf(R.id.chip1, R.id.chip2, R.id.chip3, R.id.chip4)
        val labelIds = listOf(R.id.chip1Label, R.id.chip2Label, R.id.chip3Label, R.id.chip4Label)
        pinned.take(4).forEachIndexed { index, p ->
            views.setTextViewText(labelIds[index], p.name)
            val pi = PendingIntent.getActivity(context, appWidgetId + index + 10,
                buildFlashIntent(context, p.id),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(chipIds[index], pi)
        }

        val torchOn = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
            .getBoolean("torch_default_on", true)
        views.setTextViewText(R.id.torchStatus, if (torchOn) "ON" else "OFF")
        return views
    }

    private fun buildFlashIntent(context: Context, presetId: String): Intent {
        val repo = PresetRepository(context)
        val preset = repo.getPresetById(presetId) ?: repo.getActivePreset()
        val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
        return Intent(context, FlashActivity::class.java).apply {
            putExtra("preset_id", preset.id)
            putExtra("interval_ms", preset.intervalMs)
            putExtra("torch_enabled", prefs.getBoolean("torch_default_on", true))
            putExtra("auto_stop_ms", prefs.getString("auto_stop_ms", "90000")?.toLongOrNull() ?: 90_000L)
            putIntegerArrayListExtra("colors", ArrayList(preset.colors))
            putExtra("pattern_type", preset.patternType.name)
            putExtra("preset_name", preset.name)
            putExtra("from_widget", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
