# CrossSafe — Architecture & Project Structure

Full folder structure, class list, and how everything connects.

---

## Folder Structure

```
app/
└── src/main/
    ├── java/com/crosssafe/app/
    │   ├── MainActivity.kt               ← Home screen
    │   ├── FlashActivity.kt              ← Full-screen flash
    │   ├── OnboardingActivity.kt         ← First-launch 4-card flow
    │   ├── SettingsActivity.kt           ← Settings host
    │   ├── SettingsFragment.kt           ← PreferenceFragmentCompat
    │   ├── ManagePinnedPresetsActivity.kt← Drag-to-reorder pinned presets
    │   │
    │   ├── engine/
    │   │   ├── FlashEngine.kt            ← Core flash loop logic
    │   │   └── TorchManager.kt           ← Camera torch on/off
    │   │
    │   ├── model/
    │   │   ├── Preset.kt                 ← Preset data class + enums
    │   │   ├── FlashConfig.kt            ← Config passed to FlashEngine
    │   │   ├── FlashColors.kt            ← Color constants
    │   │   └── PrefKeys.kt               ← SharedPreferences key constants
    │   │
    │   ├── data/
    │   │   └── PresetRepository.kt       ← Load/save presets, pinning
    │   │
    │   ├── viewmodel/
    │   │   ├── MainViewModel.kt          ← Home screen state
    │   │   └── FlashViewModel.kt         ← Flash screen state + timer
    │   │
    │   ├── service/
    │   │   └── FlashService.kt           ← Foreground service (flash when app backgrounded)
    │   │
    │   ├── widget/
    │   │   ├── CrossSafeWidgetProvider.kt← AppWidgetProvider
    │   │   └── WidgetConfigActivity.kt   ← Widget configuration
    │   │
    │   ├── tile/
    │   │   └── CrossSafeTileService.kt   ← Quick Settings tile
    │   │
    │   ├── gesture/
    │   │   └── ShakeDetector.kt          ← Accelerometer shake detection
    │   │
    │   └── onboarding/
    │       ├── EpilepsyFragment.kt       ← Card 1
    │       ├── HowToUseFragment.kt       ← Card 2
    │       ├── ExitMethodsFragment.kt    ← Card 3
    │       └── WidgetTipFragment.kt      ← Card 4
    │
    ├── res/
    │   ├── layout/
    │   │   ├── activity_main.xml
    │   │   ├── activity_flash.xml
    │   │   ├── activity_onboarding.xml
    │   │   ├── activity_settings.xml
    │   │   ├── activity_widget_config.xml
    │   │   ├── activity_manage_presets.xml
    │   │   ├── fragment_epilepsy.xml
    │   │   ├── fragment_how_to_use.xml
    │   │   ├── fragment_exit_methods.xml
    │   │   ├── fragment_widget_tip.xml
    │   │   ├── item_preset_chip.xml      ← Home screen preset chip
    │   │   ├── item_preset_card.xml      ← Bottom sheet preset card
    │   │   ├── item_pinned_preset.xml    ← Manage pinned list item
    │   │   ├── view_hint_bar.xml         ← "Tap to stop" overlay
    │   │   ├── view_brightness_bar.xml   ← Brightness indicator overlay
    │   │   ├── widget_quick_go.xml
    │   │   ├── widget_preset_strip.xml
    │   │   └── widget_full.xml
    │   │
    │   ├── xml/
    │   │   ├── preferences.xml           ← Settings preference tree
    │   │   └── widget_info.xml           ← AppWidget metadata
    │   │
    │   ├── values/
    │   │   ├── strings.xml
    │   │   ├── colors.xml
    │   │   ├── dimens.xml
    │   │   ├── arrays.xml               ← Dropdown options for settings
    │   │   └── themes.xml
    │   │
    │   ├── values-hi/strings.xml        ← Hindi translations
    │   ├── values-ta/strings.xml        ← Tamil translations
    │   ├── values-te/strings.xml        ← Telugu translations
    │   ├── values-kn/strings.xml        ← Kannada translations
    │   │
    │   ├── drawable/
    │   │   ├── ic_flash.xml             ← Flash/bolt icon
    │   │   ├── ic_torch.xml
    │   │   ├── ic_settings.xml
    │   │   ├── bg_go_button.xml         ← Red circle GO button background
    │   │   ├── bg_preset_chip.xml       ← Chip background with border
    │   │   ├── bg_preset_chip_selected.xml ← Chip with glow border
    │   │   ├── widget_background.xml    ← Widget rounded background
    │   │   └── widget_go_button_bg.xml
    │   │
    │   └── raw/
    │       ├── sound_beep.mp3
    │       ├── sound_horn.mp3
    │       └── sound_siren.mp3
    │
    └── AndroidManifest.xml
```

---

## Data Flow Diagram

```
User taps GO button on MainActivity
    │
    ▼
MainActivity reads active preset from PresetRepository
    │
    ▼
Builds FlashConfig from preset + current settings
    │
    ▼
Launches FlashActivity with FlashConfig in Intent extras
    │
    ▼
FlashActivity creates FlashEngine + TorchManager
    │
    ▼
FlashEngine.start()
    ├── acquireWakeLock()
    ├── setBrightness(1.0f)
    ├── starts color loop (Handler.postDelayed)
    └── TorchManager.setEnabled(true) if torch on

During flash:
    ├── User gestures → GestureDetector → FlashEngine.setBrightness() / cyclePreset()
    ├── Shake → ShakeDetector → stopFlashAndExit()
    ├── Volume key → onKeyDown() → stopFlashAndExit()
    ├── Tap → rootView.onClick → stopFlashAndExit()
    └── Timer → CountdownJob → stopFlashAndExit()

stopFlashAndExit()
    ├── FlashEngine.stop()
    │   ├── handler.removeCallbacksAndMessages()
    │   ├── restoreBrightness()
    │   └── releaseWakeLock()
    ├── TorchManager.setEnabled(false)
    ├── cancelAutoStop()
    ├── vibrateOnce(50ms)
    └── finish() → back to MainActivity
```

---

## ViewModel State

### MainViewModel

```kotlin
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PresetRepository(application)
    val prefs = application.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)

    val pinnedPresets: LiveData<List<Preset>> = MutableLiveData(repo.getPinnedPresets())
    val activePreset: LiveData<Preset> = MutableLiveData(repo.getActivePreset())
    val torchEnabled: LiveData<Boolean> = MutableLiveData(prefs.getBoolean(PrefKeys.TORCH_DEFAULT_ON, true))
    val autoStopMs: LiveData<Long> = MutableLiveData(prefs.getLong(PrefKeys.AUTO_STOP_MS, 90_000L))
    val flashSpeed: LiveData<String> = MutableLiveData(prefs.getString(PrefKeys.FLASH_SPEED, "MEDIUM"))

    fun selectPreset(preset: Preset) { ... }
    fun setTorch(on: Boolean) { ... }
    fun buildFlashConfig(): FlashConfig { ... }
}
```

### FlashViewModel

```kotlin
class FlashViewModel : ViewModel() {
    val config: FlashConfig = ...     // set from Intent extras
    val remainingMs: LiveData<Long> = MutableLiveData()
    val isWarning: LiveData<Boolean> = MutableLiveData(false)   // true when <10s left
    val currentPresetName: LiveData<String> = MutableLiveData()
    val currentBrightness: MutableLiveData<Float> = MutableLiveData(1.0f)

    fun startCountdown(totalMs: Long) { ... }
    fun cancelCountdown() { ... }
}
```

---

## Passing FlashConfig via Intent

```kotlin
// In MainActivity — launching FlashActivity
fun startFlashActivity() {
    val config = viewModel.buildFlashConfig()
    val intent = Intent(this, FlashActivity::class.java).apply {
        putExtra("preset_id", config.presetId)
        putExtra("interval_ms", config.intervalMs)
        putExtra("torch_enabled", config.torchEnabled)
        putExtra("auto_stop_ms", config.autoStopMs)
        putIntegerArrayListExtra("colors", ArrayList(config.colors))
        putExtra("pattern_type", config.patternType.name)
    }
    startActivity(intent)
}

// In FlashActivity — reading config
private fun readConfigFromIntent(): FlashConfig {
    return FlashConfig(
        presetId = intent.getStringExtra("preset_id") ?: "police",
        colors = intent.getIntegerArrayListExtra("colors") ?: arrayListOf(FlashColors.RED, FlashColors.BLUE),
        intervalMs = intent.getLongExtra("interval_ms", 300L),
        torchEnabled = intent.getBooleanExtra("torch_enabled", true),
        autoStopMs = intent.getLongExtra("auto_stop_ms", 90_000L),
        patternType = PatternType.valueOf(intent.getStringExtra("pattern_type") ?: "ALTERNATING")
    )
}
```

---

## build.gradle Dependencies

```kotlin
dependencies {
    // Android core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // ViewPager2 (onboarding)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Preferences (settings screen)
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Coroutines (timers, async)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // JSON (custom presets)
    implementation("com.google.code.gson:gson:2.10.1")
}
```

---

## Colors & Themes (res/values/colors.xml)

```xml
<resources>
    <color name="colorBackground">#0F0F0F</color>
    <color name="colorSurface">#1A1A1A</color>
    <color name="colorSurfaceVariant">#242424</color>
    <color name="colorPrimary">#EF4444</color>
    <color name="colorPrimaryDark">#B91C1C</color>
    <color name="colorText">#FFFFFF</color>
    <color name="colorTextSecondary">#9CA3AF</color>
    <color name="colorAccentBlue">#60A5FA</color>
    <color name="colorAccentAmber">#F59E0B</color>
    <color name="colorAccentGreen">#22C55E</color>
    <color name="colorWarning">#F59E0B</color>
    <color name="colorDanger">#EF4444</color>
    <color name="colorSuccess">#22C55E</color>
    <color name="chipBorderSelected">#60A5FA</color>
</resources>
```

---

## Dimensions (res/values/dimens.xml)

```xml
<resources>
    <dimen name="go_button_size">140dp</dimen>
    <dimen name="preset_chip_height">56dp</dimen>
    <dimen name="preset_chip_radius">12dp</dimen>
    <dimen name="card_radius">12dp</dimen>
    <dimen name="screen_padding">16dp</dimen>
    <dimen name="text_huge">48sp</dimen>      <!-- flash countdown -->
    <dimen name="text_go_button">18sp</dimen>
    <dimen name="text_body">16sp</dimen>
    <dimen name="text_small">13sp</dimen>
    <dimen name="text_tiny">11sp</dimen>
</resources>
```
