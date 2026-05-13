# CrossSafe — Home Screen Widgets

3 widget sizes. Each independently configurable. Tap launches flash instantly.

---

## Widget Sizes

| Widget | Size | What it shows |
|---|---|---|
| QuickGo | 1×1 | Single GO button, uses last preset |
| PresetStrip | 2×1 | GO button + 2 preset shortcuts |
| FullWidget | 2×2 | GO button + 4 presets + torch toggle |

---

## AppWidgetProvider Class

```kotlin
// Location: app/src/main/java/com/crosssafe/app/widget/CrossSafeWidgetProvider.kt

class CrossSafeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
        val presetId = prefs.getString("preset_id", "police") ?: "police"
        val widgetType = prefs.getString("widget_type", "quick_go") ?: "quick_go"

        val views = when (widgetType) {
            "preset_strip" -> buildPresetStripWidget(context, appWidgetId, presetId)
            "full_widget"  -> buildFullWidget(context, appWidgetId, presetId)
            else           -> buildQuickGoWidget(context, appWidgetId, presetId)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
```

---

## Widget 1 — QuickGo (1×1)

### Layout: `widget_quick_go.xml`

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background"
    android:padding="8dp">

    <ImageView
        android:id="@+id/widgetIcon"
        android:layout_width="36dp"
        android:layout_height="36dp"
        android:layout_centerHorizontal="true"
        android:layout_above="@id/widgetLabel"
        android:src="@drawable/ic_flash" />

    <TextView
        android:id="@+id/widgetLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_marginTop="4dp"
        android:text="GO"
        android:textColor="#FFFFFF"
        android:textSize="14sp"
        android:textStyle="bold" />

</RelativeLayout>
```

### Build QuickGo Widget

```kotlin
private fun buildQuickGoWidget(
    context: Context,
    appWidgetId: Int,
    presetId: String
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_quick_go)

    // Set tap action — launches FlashActivity with preset
    val intent = Intent(context, FlashActivity::class.java).apply {
        putExtra("preset_id", presetId)
        putExtra("from_widget", true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, appWidgetId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

    return views
}
```

---

## Widget 2 — PresetStrip (2×1)

### Layout: `widget_preset_strip.xml`

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background"
    android:padding="8dp"
    android:gravity="center_vertical">

    <!-- GO button (left, larger) -->
    <LinearLayout
        android:id="@+id/btnGoMain"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1.5"
        android:orientation="vertical"
        android:gravity="center"
        android:background="@drawable/widget_go_button_bg">

        <TextView
            android:text="GO"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/mainPresetName"
            android:textColor="#CCFFFFFF"
            android:textSize="10sp" />
    </LinearLayout>

    <View android:layout_width="6dp" android:layout_height="0dp" />

    <!-- Preset shortcut 1 -->
    <LinearLayout
        android:id="@+id/btnPreset1"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:orientation="vertical"
        android:gravity="center"
        android:background="@drawable/widget_preset_bg">

        <TextView
            android:id="@+id/preset1Emoji"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/preset1Name"
            android:textColor="#FFFFFF"
            android:textSize="9sp" />
    </LinearLayout>

    <!-- Preset shortcut 2 -->
    <LinearLayout
        android:id="@+id/btnPreset2"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:orientation="vertical"
        android:gravity="center"
        android:background="@drawable/widget_preset_bg">

        <TextView android:id="@+id/preset2Emoji" android:textSize="16sp" />
        <TextView android:id="@+id/preset2Name" android:textColor="#FFFFFF" android:textSize="9sp" />
    </LinearLayout>

</LinearLayout>
```

---

## Widget 3 — FullWidget (2×2)

### Layout: `widget_full.xml`

```
┌──────────────────────────┐
│  ┌──────────────────┐    │
│  │   ●  GO          │    │  ← big GO button top
│  │  [Police preset] │    │
│  └──────────────────┘    │
│                          │
│  [🔵🔴] [🔴] [⚪] [🟡]  │  ← 4 preset quick-launch buttons in a row
│                          │
│  🔦 Torch: [──●──] ON   │  ← torch toggle row
└──────────────────────────┘
```

---

## Widget Configuration Activity

Opens when user first adds a widget or long-presses → Configure.

```kotlin
// Location: app/src/main/java/com/crosssafe/app/widget/WidgetConfigActivity.kt

class WidgetConfigActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        // Get the widget ID from the intent
        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If no valid ID, cancel
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // User picks:
    // - Widget type (QuickGo / PresetStrip / FullWidget)
    // - Default preset
    // - Which presets appear in preset slots
    // - Widget theme (dark / transparent)

    fun saveAndFinish() {
        val prefs = getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("preset_id", selectedPresetId)
            .putString("widget_type", selectedWidgetType)
            .apply()

        // Trigger widget update
        val appWidgetManager = AppWidgetManager.getInstance(this)
        CrossSafeWidgetProvider().onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))

        // Return success
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
```

---

## AndroidManifest — Widget Entries

```xml
<receiver
    android:name=".widget.CrossSafeWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/widget_info" />
</receiver>

<activity
    android:name=".widget.WidgetConfigActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

---

## res/xml/widget_info.xml

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="40dp"
    android:minHeight="40dp"
    android:targetCellWidth="1"
    android:targetCellHeight="1"
    android:maxResizeWidth="250dp"
    android:maxResizeHeight="250dp"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="0"
    android:initialLayout="@layout/widget_quick_go"
    android:configure="com.crosssafe.app.widget.WidgetConfigActivity"
    android:widgetCategory="home_screen"
    android:description="@string/widget_description" />
```

---

## Quick Settings Tile

```kotlin
// Location: app/src/main/java/com/crosssafe/app/tile/CrossSafeTileService.kt

class CrossSafeTileService : TileService() {
    override fun onClick() {
        // Launch flash directly from Quick Settings shade
        val intent = Intent(this, FlashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivityAndCollapse(intent)
    }

    override fun onStartListening() {
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.label = "CrossSafe"
        qsTile.updateTile()
    }
}
```

```xml
<!-- In AndroidManifest.xml -->
<service
    android:name=".tile.CrossSafeTileService"
    android:exported="true"
    android:icon="@drawable/ic_crosssafe_tile"
    android:label="CrossSafe"
    android:permission="android.permission.BIND_QUICK_SETTINGS_TILE">
    <intent-filter>
        <action android:name="android.service.quicksettings.action.QS_TILE" />
    </intent-filter>
</service>
```
