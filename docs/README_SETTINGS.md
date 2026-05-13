# CrossSafe — Settings

Every configurable option, where it's stored, and its default value.

---

## SharedPreferences Keys — Full List

All settings stored in `"crosssafe_prefs"` SharedPreferences file.

```kotlin
object PrefKeys {
    // Onboarding
    const val ONBOARDING_COMPLETE   = "onboarding_complete"       // Boolean, default: false

    // Presets
    const val LAST_PRESET_ID        = "last_preset_id"            // String, default: "police"
    const val PINNED_PRESET_IDS     = "pinned_preset_ids"         // String (CSV), default: "police,red_only,white_only,amber_only"
    const val DEFAULT_PRESET_ID     = "default_preset_id"         // String, default: "police"
    const val CUSTOM_PRESETS_JSON   = "custom_presets_json"       // String (JSON array), default: "[]"

    // Flash behaviour
    const val DEFAULT_BRIGHTNESS    = "default_brightness"        // Float, default: 1.0f (100%)
    const val AUTO_STOP_MS          = "auto_stop_ms"              // Long, default: 90_000L
    const val FLASH_SPEED           = "flash_speed"               // String, default: "MEDIUM"
    const val TORCH_DEFAULT_ON      = "torch_default_on"          // Boolean, default: true
    const val RESTORE_BRIGHTNESS    = "restore_brightness"        // Boolean, default: true

    // Triggers
    const val SHAKE_TO_START        = "shake_to_start"            // Boolean, default: false
    const val SHAKE_SENSITIVITY     = "shake_sensitivity"         // Int (1–10), default: 6
    const val VOLUME_TRIGGER        = "volume_trigger"            // Boolean, default: false

    // Sound
    const val AUDIO_ALERT           = "audio_alert"               // String, default: "NONE"
    const val AUDIO_VOLUME          = "audio_volume"              // Int (0–100), default: 70

    // Accessibility
    const val TEXT_SIZE             = "text_size"                 // String, default: "NORMAL"
    const val HIGH_CONTRAST         = "high_contrast"             // Boolean, default: false
    const val REDUCE_MOTION         = "reduce_motion"             // Boolean, default: false

    // Language
    const val APP_LANGUAGE          = "app_language"              // String, default: "en"
}
```

---

## SettingsActivity

```kotlin
// Location: app/src/main/java/com/crosssafe/app/SettingsActivity.kt

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()
    }
}
```

---

## SettingsFragment (PreferenceFragmentCompat)

```kotlin
// Location: app/src/main/java/com/crosssafe/app/SettingsFragment.kt

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupPresets()
        setupTriggers()
        setupFlashBehaviour()
        setupSound()
        setupAccessibility()
        setupLanguage()
        setupHelp()
    }
}
```

---

## res/xml/preferences.xml (full structure)

```xml
<PreferenceScreen xmlns:app="http://schemas.android.com/apk/res-auto">

    <!-- PRESETS -->
    <PreferenceCategory app:title="Presets">

        <Preference
            app:key="manage_pinned"
            app:title="Manage pinned presets"
            app:summary="Drag to reorder your 4 home screen presets" />

        <ListPreference
            app:key="default_preset_id"
            app:title="Default preset on open"
            app:summary="%s"
            app:defaultValue="police" />

        <Preference
            app:key="create_custom_preset"
            app:title="Create custom preset"
            app:summary="Build your own color combination" />

    </PreferenceCategory>

    <!-- TRIGGERS -->
    <PreferenceCategory app:title="Quick triggers">

        <SwitchPreferenceCompat
            app:key="shake_to_start"
            app:title="Shake to start"
            app:summary="Shake phone 3 times to start flashing"
            app:defaultValue="false" />

        <SeekBarPreference
            app:key="shake_sensitivity"
            app:title="Shake sensitivity"
            app:min="1"
            app:max="10"
            app:defaultValue="6"
            app:dependency="shake_to_start" />

        <SwitchPreferenceCompat
            app:key="volume_trigger"
            app:title="Volume button trigger"
            app:summary="Hold Volume Down 2 seconds to start"
            app:defaultValue="false" />

        <Preference
            app:key="quick_tile_info"
            app:title="Quick Settings tile"
            app:summary="Add CrossSafe to your notification shade pulldown" />

    </PreferenceCategory>

    <!-- FLASH BEHAVIOUR -->
    <PreferenceCategory app:title="Flash behaviour">

        <SeekBarPreference
            app:key="default_brightness"
            app:title="Default brightness"
            app:summary="Screen brightness during flash"
            app:min="50"
            app:max="100"
            app:defaultValue="100" />

        <ListPreference
            app:key="auto_stop_ms"
            app:title="Auto-stop timer"
            app:entries="@array/auto_stop_labels"
            app:entryValues="@array/auto_stop_values"
            app:summary="%s"
            app:defaultValue="90000" />

        <ListPreference
            app:key="flash_speed"
            app:title="Default flash speed"
            app:entries="@array/speed_labels"
            app:entryValues="@array/speed_values"
            app:summary="%s"
            app:defaultValue="MEDIUM" />

        <SwitchPreferenceCompat
            app:key="torch_default_on"
            app:title="Torch on by default"
            app:summary="Fire camera flash in sync with screen"
            app:defaultValue="true" />

        <SwitchPreferenceCompat
            app:key="restore_brightness"
            app:title="Restore brightness on exit"
            app:summary="Return to original brightness after flash stops"
            app:defaultValue="true" />

    </PreferenceCategory>

    <!-- SOUND -->
    <PreferenceCategory app:title="Sound">

        <ListPreference
            app:key="audio_alert"
            app:title="Audio alert"
            app:entries="@array/audio_labels"
            app:entryValues="@array/audio_values"
            app:summary="%s"
            app:defaultValue="NONE" />

        <SeekBarPreference
            app:key="audio_volume"
            app:title="Audio volume"
            app:min="0"
            app:max="100"
            app:defaultValue="70"
            app:dependency="audio_alert" />

    </PreferenceCategory>

    <!-- ACCESSIBILITY -->
    <PreferenceCategory app:title="Accessibility">

        <ListPreference
            app:key="text_size"
            app:title="Text size"
            app:entries="@array/text_size_labels"
            app:entryValues="@array/text_size_values"
            app:summary="%s"
            app:defaultValue="NORMAL" />

        <SwitchPreferenceCompat
            app:key="high_contrast"
            app:title="High contrast mode"
            app:summary="Stronger color contrast throughout the app"
            app:defaultValue="false" />

        <SwitchPreferenceCompat
            app:key="reduce_motion"
            app:title="Reduce motion"
            app:summary="Disable animations in the app"
            app:defaultValue="false" />

    </PreferenceCategory>

    <!-- LANGUAGE -->
    <PreferenceCategory app:title="Language">

        <ListPreference
            app:key="app_language"
            app:title="App language"
            app:entries="@array/language_labels"
            app:entryValues="@array/language_values"
            app:summary="%s"
            app:defaultValue="en" />

    </PreferenceCategory>

    <!-- HELP & ABOUT -->
    <PreferenceCategory app:title="Help and about">

        <Preference
            app:key="replay_onboarding"
            app:title="How to use"
            app:summary="See the app tutorial again" />

        <Preference
            app:key="epilepsy_warning"
            app:title="Epilepsy warning"
            app:summary="View the safety warning" />

        <Preference
            app:key="privacy_policy"
            app:title="Privacy policy"
            app:summary="No data collected. 100% offline." />

        <Preference
            app:key="app_version"
            app:title="Version"
            app:summary="1.0.0" />

    </PreferenceCategory>

</PreferenceScreen>
```

---

## Array Resources (res/values/arrays.xml)

```xml
<resources>
    <!-- Auto-stop labels and values -->
    <string-array name="auto_stop_labels">
        <item>Off (no limit)</item>
        <item>30 seconds</item>
        <item>60 seconds</item>
        <item>90 seconds</item>
        <item>2 minutes</item>
        <item>3 minutes</item>
    </string-array>
    <string-array name="auto_stop_values">
        <item>0</item>
        <item>30000</item>
        <item>60000</item>
        <item>90000</item>
        <item>120000</item>
        <item>180000</item>
    </string-array>

    <!-- Flash speed -->
    <string-array name="speed_labels">
        <item>Pulse (very slow)</item>
        <item>Slow</item>
        <item>Medium</item>
        <item>Fast</item>
        <item>Rapid (strobe)</item>
    </string-array>
    <string-array name="speed_values">
        <item>PULSE</item>
        <item>SLOW</item>
        <item>MEDIUM</item>
        <item>FAST</item>
        <item>RAPID</item>
    </string-array>

    <!-- Audio alert -->
    <string-array name="audio_labels">
        <item>None (silent)</item>
        <item>Beep</item>
        <item>Horn</item>
        <item>Siren</item>
    </string-array>
    <string-array name="audio_values">
        <item>NONE</item>
        <item>BEEP</item>
        <item>HORN</item>
        <item>SIREN</item>
    </string-array>

    <!-- Text size -->
    <string-array name="text_size_labels">
        <item>Normal</item>
        <item>Large</item>
        <item>Extra large</item>
    </string-array>
    <string-array name="text_size_values">
        <item>NORMAL</item>
        <item>LARGE</item>
        <item>XLARGE</item>
    </string-array>

    <!-- Language -->
    <string-array name="language_labels">
        <item>English</item>
        <item>हिंदी (Hindi)</item>
        <item>தமிழ் (Tamil)</item>
        <item>తెలుగు (Telugu)</item>
        <item>ಕನ್ನಡ (Kannada)</item>
        <item>मराठी (Marathi)</item>
        <item>বাংলা (Bengali)</item>
        <item>ગુજરાતી (Gujarati)</item>
    </string-array>
    <string-array name="language_values">
        <item>en</item>
        <item>hi</item>
        <item>ta</item>
        <item>te</item>
        <item>kn</item>
        <item>mr</item>
        <item>bn</item>
        <item>gu</item>
    </string-array>
</resources>
```

---

## Manage Pinned Presets Screen

Custom activity/fragment with drag-to-reorder list.

```kotlin
// Location: app/src/main/java/com/crosssafe/app/ManagePinnedPresetsActivity.kt

// Uses ItemTouchHelper for drag-to-reorder
// Shows 4 pinned presets in a RecyclerView
// Drag handle on right side of each row
// Swipe left to unpin (replaces with "empty" slot)
// "Add preset" button to fill empty slots from the preset library

class ManagePinnedPresetsActivity : AppCompatActivity() {
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
    ) {
        override fun onMove(...): Boolean { /* swap items in list */ return true }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            /* unpin preset at this position */
        }
    })
}
```
