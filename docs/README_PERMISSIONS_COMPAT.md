# CrossSafe — Permissions, Compatibility & Android System Integration

Everything needed to support all Android skins, all screen sizes, all permissions, in-app updates, and system-level best practices.

---

## IMPORTANT: Camera Permission Not Required

**CrossSafe does NOT need the CAMERA permission** to control the flashlight/torch on devices running Android 8.0+ (API 26+).

The `CameraManager.setTorchMode()` API can control the torch without requesting camera permission. Camera permission is only required if you're actually opening the camera to capture photos/video.

Since our minSdk is 26, we can safely use the torch without any permission requests.

---

## 1. All Permissions (AndroidManifest.xml)

### Required Permissions
```xml
<!-- WAKE_LOCK — keeps screen ON during flash, prevents auto-sleep -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- VIBRATE — haptic feedback on GO tap, exit confirmation -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- FOREGROUND_SERVICE — keep flash running if user presses Home -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />


<!-- POST_NOTIFICATIONS — show "CrossSafe active" notification on Android 13+ -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- RECEIVE_BOOT_COMPLETED — restore widget state after phone restarts -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- SCHEDULE_EXACT_ALARM — precise auto-stop timer on Android 12+ -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

### Optional / Soft Permissions
```xml
<!-- REQUEST_INSTALL_PACKAGES — for in-app update installation (if self-hosted) -->
<!-- Only needed if NOT using Play Store in-app updates -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- WRITE_SETTINGS — to control system brightness directly (alternative approach) -->
<!-- Most devices don't need this if using WindowManager brightness -->
<!-- <uses-permission android:name="android.permission.WRITE_SETTINGS" /> -->
```

### Hardware Features (mark flash as NOT required so app installs on tablets without flash)
```xml
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />

<uses-feature
    android:name="android.hardware.camera.flash"
    android:required="false" />

<uses-feature
    android:name="android.hardware.sensor.accelerometer"
    android:required="false" />
```

---

## 2. Runtime Permission Requests

Not all permissions can be declared in manifest — some must be asked at runtime.

### PermissionManager.kt
```kotlin
// Location: app/src/main/java/com/crosssafe/app/util/PermissionManager.kt

class PermissionManager(private val activity: AppCompatActivity) {

    // POST_NOTIFICATIONS — Android 13+ only
    fun requestNotificationIfNeeded(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onGranted()
            }
        } else {
            onGranted() // Not needed below Android 13
        }
    }

    private val notificationLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* notification permission result — non-blocking */ }

    // Check if torch is available on this device at all
    fun isTorchAvailable(): Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    // If permission permanently denied — send user to App Settings
    fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }
}
```

### When to Ask Each Permission

| Permission | When to ask | Blocking? |
|---|---|---|
| POST_NOTIFICATIONS | On app first launch (after onboarding) | No — silent if denied |
| SCHEDULE_EXACT_ALARM | Only on Android 12+ when auto-stop is set | No — fall back to inexact alarm |

### Permission Flow in MainActivity
```kotlin
override fun onCreate(...) {
    // Ask notification permission early (non-blocking)
    permissionManager.requestNotificationIfNeeded {}
}

fun onGoButtonTapped() {
    // No permission checks needed — torch works without camera permission on API 26+
    startFlashActivity()
}
```

---

## 3. In-App Updates (Google Play In-App Update API)

Shows an update prompt inside the app without forcing users to go to Play Store.

### build.gradle dependency
```kotlin
implementation("com.google.android.play:app-update-ktx:2.1.0")
```

### UpdateManager.kt
```kotlin
// Location: app/src/main/java/com/crosssafe/app/util/UpdateManager.kt

class UpdateManager(private val activity: AppCompatActivity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val UPDATE_REQUEST_CODE = 500

    fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            when {
                // IMMEDIATE update — critical security fix or major version
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.updatePriority() >= 4  // priority 4-5 = force update
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    startImmediateUpdate(appUpdateInfo)
                }

                // FLEXIBLE update — minor improvements (recommended, not forced)
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    startFlexibleUpdate(appUpdateInfo)
                }

                // Update already downloaded — prompt to install
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                    showUpdateReadySnackbar()
                }
            }
        }
    }

    // IMMEDIATE — fullscreen blocker, cannot dismiss (use for critical updates only)
    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            activity,
            UPDATE_REQUEST_CODE
        )
    }

    // FLEXIBLE — download in background, user sees a banner
    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            activity,
            UPDATE_REQUEST_CODE
        )

        // Listen for download completion
        appUpdateManager.registerListener(installStateListener)
    }

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> showUpdateReadySnackbar()
            InstallStatus.INSTALLED  -> appUpdateManager.unregisterListener(installStateListener)
            InstallStatus.FAILED     -> { /* log error, retry later */ }
            else -> {}
        }
    }

    // Show a snackbar at the bottom: "Update ready — Restart to install"
    private fun showUpdateReadySnackbar() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "Update ready! Restart CrossSafe to install.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart now") {
            appUpdateManager.completeUpdate()
        }.setActionTextColor(activity.getColor(R.color.colorPrimary))
         .show()
    }

    // Call in onActivityResult / onResume for immediate updates
    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume interrupted immediate update
                startImmediateUpdate(appUpdateInfo)
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateListener)
    }
}
```

### In-App Changelog Dialog

Show what's new when the app updates.

```kotlin
// Location: app/src/main/java/com/crosssafe/app/util/ChangelogManager.kt

class ChangelogManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)

    fun showChangelogIfUpdated(activity: AppCompatActivity) {
        val currentVersion = BuildConfig.VERSION_CODE
        val lastSeenVersion = prefs.getInt("last_seen_version", 0)

        if (currentVersion > lastSeenVersion) {
            prefs.edit().putInt("last_seen_version", currentVersion).apply()
            showChangelogDialog(activity)
        }
    }

    private fun showChangelogDialog(activity: AppCompatActivity) {
        // Build the changelog from a string resource (easy to update each release)
        MaterialAlertDialogBuilder(activity)
            .setTitle("What's new in CrossSafe ${BuildConfig.VERSION_NAME}")
            .setMessage(getChangelogText())
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun getChangelogText(): String {
        // Read from res/raw/changelog.txt or hardcode per version
        return context.getString(R.string.changelog_current_version)
    }
}

// In strings.xml — update this every release:
// <string name="changelog_current_version">
//   • 6 new presets added\n
//   • Shake sensitivity improved\n
//   • Bug fix: torch stays on during preset switch\n
//   • Widget now supports transparent background
// </string>
```

### Call UpdateManager in MainActivity
```kotlin
private lateinit var updateManager: UpdateManager
private lateinit var changelogManager: ChangelogManager

override fun onCreate(...) {
    updateManager = UpdateManager(this)
    changelogManager = ChangelogManager(this)

    updateManager.checkForUpdate()
    changelogManager.showChangelogIfUpdated(this)
}

override fun onResume() {
    super.onResume()
    updateManager.onResume()
}

override fun onDestroy() {
    super.onDestroy()
    updateManager.onDestroy()
}
```

---

## 4. Screen Size & Display Compatibility

### Support All Screen Sizes

```xml
<!-- In AndroidManifest.xml -->
<supports-screens
    android:smallScreens="true"
    android:normalScreens="true"
    android:largeScreens="true"
    android:xlargeScreens="true"
    android:resizeable="true"
    android:anyDensity="true" />
```

### Responsive GO Button (scales with screen)

Never use a fixed `140dp` — use `ConstraintLayout` with percentage-based sizing:

```xml
<!-- activity_main.xml -->
<androidx.constraintlayout.widget.ConstraintLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnGo"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintWidth_percent="0.45"
        app:layout_constraintDimensionRatio="1:1"
        app:layout_constraintTop_toBottomOf="@id/presetsGrid"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toTopOf="@id/controlsRow"
        android:text="START\nPOLICE"
        android:textSize="18sp"
        app:cornerRadius="999dp"
        android:backgroundTint="@color/colorPrimary" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Density Buckets — drawable folders

```
res/
├── drawable-mdpi/      ← 1x (160dpi)
├── drawable-hdpi/      ← 1.5x (240dpi)
├── drawable-xhdpi/     ← 2x (320dpi)
├── drawable-xxhdpi/    ← 3x (480dpi)
├── drawable-xxxhdpi/   ← 4x (640dpi)
└── drawable/           ← vector drawables (preferred — scale to any size)
```

Use vector drawables (`ic_*.xml`) for all icons — they scale perfectly to any density.

### Layout Variants

```
res/
├── layout/               ← phones (portrait, default)
├── layout-land/          ← phones (landscape)
├── layout-sw600dp/       ← tablets 7"+ (smallest width 600dp)
├── layout-sw720dp/       ← tablets 10"+
└── layout-w900dp/        ← wide layouts (foldables unfolded)
```

For tablets, make the home screen 2-column with GO button on left, presets on right:

```xml
<!-- layout-sw600dp/activity_main.xml -->
<LinearLayout android:orientation="horizontal">
    <!-- Left: GO button -->
    <FrameLayout android:layout_width="0dp" android:layout_weight="1">
        <!-- GO button centered here -->
    </FrameLayout>
    <!-- Right: presets + controls -->
    <LinearLayout android:layout_width="0dp" android:layout_weight="1"
        android:orientation="vertical">
        <!-- Preset grid + quick controls -->
    </LinearLayout>
</LinearLayout>
```

### Foldable Support

```kotlin
// In MainActivity — handle fold/unfold state changes
class MainActivity : AppCompatActivity() {

    override fun onCreate(...) {
        // Tell the system this activity can resize when folded/unfolded
        // No special code needed if layout is properly responsive
    }

    // Opt into foldable multi-window
    // In AndroidManifest:
    // android:resizeableActivity="true"
    // android:supportsPictureInPicture="false"
}
```

```xml
<!-- AndroidManifest.xml — on MainActivity and FlashActivity -->
android:resizeableActivity="true"
android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize|screenSize"
```

---

## 5. Android Skin Compatibility (One UI, MIUI, ColorOS, etc.)

Different Android skins have different behaviours. Handle all of them.

### Brightness Control — Skin-Safe Approach

Some skins (MIUI, ColorOS) block `WindowManager` brightness changes. Use a two-method approach:

```kotlin
fun setBrightness(window: Window, level: Float) {
    // Method 1: WindowManager (works on stock Android, One UI, Pixel)
    val params = window.attributes
    params.screenBrightness = level.coerceIn(0.01f, 1.0f)
    window.attributes = params

    // Method 2: System Settings fallback (needs WRITE_SETTINGS on some skins)
    // Only use if WindowManager method fails or is ignored
    try {
        if (Settings.System.canWrite(context)) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (level * 255).toInt()
            )
        }
    } catch (e: Exception) {
        // Silently ignore — WindowManager method is the primary
    }
}
```

### Torch Compatibility

```kotlin
// Some skins (MIUI) have their own torch management — CameraManager still works
// but may conflict if the user's torch is already on.
// Always wrap in try/catch and handle CameraAccessException gracefully.

fun setTorch(on: Boolean) {
    try {
        cameraManager.setTorchMode(cameraId, on)
    } catch (e: CameraAccessException) {
        when (e.reason) {
            CameraAccessException.CAMERA_IN_USE -> {
                // Another app is using the camera — show a brief toast
                showToast("Torch unavailable — camera in use")
            }
            CameraAccessException.MAX_CAMERAS_IN_USE -> {
                showToast("Torch unavailable — too many camera apps open")
            }
            else -> {
                // Log and continue without torch
            }
        }
    } catch (e: IllegalArgumentException) {
        // Device has no flash — silently disable torch option
        disableTorchUI()
    }
}
```

### Battery Optimization — All Skins

Aggressive battery savers on MIUI, One UI, and ColorOS kill background apps and foreground services.

```kotlin
// In MainActivity or OnboardingActivity — ask user to whitelist the app

fun checkBatteryOptimization() {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = packageName

    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Allow background activity")
            .setMessage(
                "To keep the flash running reliably when you press Home, " +
                "please allow CrossSafe to run in the background.\n\n" +
                "On the next screen, tap 'Allow' or 'Don't optimize'."
            )
            .setPositiveButton("Open settings") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Skip", null)
            .show()
    }
}
```

### Autostart Permission (MIUI, ColorOS, One UI)

These skins require users to manually grant "autostart" permission. Show a guide:

```kotlin
// DetectDeviceSkin.kt
object DeviceSkin {
    val isMIUI: Boolean get() = !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()
    val isOneUI: Boolean get() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    val isColorOS: Boolean get() = Build.MANUFACTURER.equals("oppo", ignoreCase = true)
        || Build.MANUFACTURER.equals("realme", ignoreCase = true)
    val isFunTouchOS: Boolean get() = Build.MANUFACTURER.equals("vivo", ignoreCase = true)
    val isOxygenOS: Boolean get() = Build.MANUFACTURER.equals("oneplus", ignoreCase = true)

    private fun getSystemProperty(key: String): String? = try {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
            .invoke(null, key) as? String
    } catch (e: Exception) { null }
}

// Show skin-specific guidance in onboarding or settings:
fun showAutostartGuidance(context: Context) {
    val message = when {
        DeviceSkin.isMIUI -> "On Xiaomi/MIUI: Settings → Apps → CrossSafe → Other permissions → Autostart → ON"
        DeviceSkin.isOneUI -> "On Samsung One UI: Settings → Device care → Battery → Background usage limits → Never sleeping apps → Add CrossSafe"
        DeviceSkin.isColorOS -> "On ColorOS/Realme: Settings → Battery → App energy efficiency → CrossSafe → Allow background run"
        DeviceSkin.isFunTouchOS -> "On Vivo: Settings → Battery → Background app management → CrossSafe → Allow"
        else -> "On your device: go to Settings → Apps → CrossSafe → Battery → Unrestricted"
    }

    MaterialAlertDialogBuilder(context)
        .setTitle("Keep CrossSafe reliable")
        .setMessage("$message\n\nThis ensures the flash keeps running when you press Home.")
        .setPositiveButton("OK", null)
        .show()
}
```

### Immersive Mode — All Android Versions

```kotlin
// FlashActivity — hide status bar and navigation bar safely across all Android versions

fun enterImmersiveMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+ (API 30+) — new WindowInsetsController API
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        // Android 8–10 — legacy flags
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}

// Call this in onWindowFocusChanged too (some skins reset immersive mode on focus change)
override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) enterImmersiveMode()
}
```

### Show Over Lock Screen

Flash should show even when phone is locked (in pocket):

```kotlin
// In FlashActivity.onCreate()
fun setupShowOnLockScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        // Android 8.1+ (API 27+)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }
}
```

---

## 6. Edge-to-Edge & Modern Android UI

```kotlin
// In all Activities (Android 15 now enforces edge-to-edge)

override fun onCreate(...) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()  // androidx.activity:activity:1.8.0+
    setContentView(...)

    // Handle insets so content isn't hidden behind status/nav bars
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}
```

### FlashActivity — full bleed (no padding — flash should cover entire screen)
```kotlin
// FlashActivity — flash covers the whole screen including under bars
override fun onCreate(...) {
    enableEdgeToEdge()
    setContentView(...)
    // DO NOT apply window insets padding — flash must be full screen
    window.setDecorFitsSystemWindows(false)
}
```

---

## 7. Adaptive Icons (all Android skins render differently)

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <!-- Monochrome for Android 13+ themed icons -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

- `ic_launcher_background`: red color `#EF4444`
- `ic_launcher_foreground`: white lightning bolt / shield icon (vector)
- `ic_launcher_monochrome`: same icon in single color (for system-themed icons)

Provide all sizes:
```
mipmap-mdpi/ic_launcher.png        48×48
mipmap-hdpi/ic_launcher.png        72×72
mipmap-xhdpi/ic_launcher.png       96×96
mipmap-xxhdpi/ic_launcher.png      144×144
mipmap-xxxhdpi/ic_launcher.png     192×192
mipmap-anydpi-v26/ic_launcher.xml  (adaptive icon)
```

---

## 8. Widget UI — Icons & Visual Polish

### Widget Background Drawables

```xml
<!-- res/drawable/widget_background.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#CC0F0F0F" />   <!-- semi-transparent dark -->
    <corners android:radius="16dp" />
    <stroke android:width="0.5dp" android:color="#33FFFFFF" />
</shape>

<!-- res/drawable/widget_background_transparent.xml -->
<shape android:shape="rectangle">
    <solid android:color="#880F0F0F" />   <!-- more transparent -->
    <corners android:radius="16dp" />
</shape>

<!-- res/drawable/widget_go_button_bg.xml — the red GO circle -->
<shape android:shape="oval">
    <solid android:color="#EF4444" />
    <stroke android:width="2dp" android:color="#B91C1C" />
</shape>

<!-- res/drawable/widget_preset_chip_bg.xml -->
<shape android:shape="rectangle">
    <solid android:color="#221A1A1A" />
    <corners android:radius="10dp" />
    <stroke android:width="0.5dp" android:color="#44FFFFFF" />
</shape>
```

### Widget Icons (vector drawables)

Create these in `res/drawable/`:

```
ic_widget_go.xml         — filled lightning bolt or play arrow (white, 24dp)
ic_widget_torch.xml      — flashlight/torch icon (white, 20dp)
ic_widget_police.xml     — shield with star (blue, 20dp)
ic_widget_red.xml        — filled circle (red)
ic_widget_white.xml      — filled circle (white)
ic_widget_amber.xml      — filled circle (amber)
ic_widget_settings.xml   — gear icon (gray, 16dp)
ic_crosssafe_tile.xml    — simple bold flash bolt (for Quick Settings tile)
ic_notification.xml      — small monochrome flash icon (for notification bar)
```

### Widget 1×1 — Enhanced Layout

```xml
<!-- res/layout/widget_quick_go.xml -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background"
    android:padding="6dp">

    <!-- Red circle background -->
    <FrameLayout
        android:id="@+id/widgetGoCircle"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/widget_go_button_bg"
        android:padding="8dp">

        <!-- Lightning bolt icon -->
        <ImageView
            android:layout_width="28dp"
            android:layout_height="28dp"
            android:layout_gravity="center"
            android:layout_marginBottom="14dp"
            android:src="@drawable/ic_widget_go"
            android:tint="#FFFFFF"
            android:contentDescription="Flash" />

        <!-- "GO" text -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:layout_marginBottom="6dp"
            android:text="GO"
            android:textColor="#FFFFFF"
            android:textSize="12sp"
            android:textStyle="bold"
            android:letterSpacing="0.08" />

    </FrameLayout>

</RelativeLayout>
```

### Widget 2×1 — Enhanced Layout

```xml
<!-- res/layout/widget_preset_strip.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background"
    android:orientation="horizontal"
    android:padding="8dp"
    android:gravity="center_vertical">

    <!-- Main GO button (wider) -->
    <LinearLayout
        android:id="@+id/btnGoMain"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1.6"
        android:orientation="vertical"
        android:gravity="center"
        android:background="@drawable/widget_go_button_bg"
        android:padding="4dp">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_widget_go"
            android:tint="#FFFFFF"
            android:contentDescription="Flash" />

        <TextView
            android:layout_marginTop="2dp"
            android:text="GO"
            android:textColor="#FFFFFF"
            android:textSize="13sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/mainPresetName"
            android:textColor="#BBFFFFFF"
            android:textSize="9sp"
            android:maxLines="1"
            android:ellipsize="end" />
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
        android:background="@drawable/widget_preset_chip_bg">

        <ImageView
            android:id="@+id/preset1Icon"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:src="@drawable/ic_widget_police"
            android:contentDescription="Preset 1" />

        <TextView
            android:id="@+id/preset1Name"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:layout_marginTop="3dp"
            android:maxLines="1"
            android:ellipsize="end" />
    </LinearLayout>

    <View android:layout_width="4dp" android:layout_height="0dp" />

    <!-- Preset shortcut 2 -->
    <LinearLayout
        android:id="@+id/btnPreset2"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:orientation="vertical"
        android:gravity="center"
        android:background="@drawable/widget_preset_chip_bg">

        <ImageView
            android:id="@+id/preset2Icon"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:src="@drawable/ic_widget_amber"
            android:contentDescription="Preset 2" />

        <TextView
            android:id="@+id/preset2Name"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:layout_marginTop="3dp"
            android:maxLines="1"
            android:ellipsize="end" />
    </LinearLayout>

</LinearLayout>
```

### Widget 2×2 — Enhanced Layout

```xml
<!-- res/layout/widget_full.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/widget_background"
    android:orientation="vertical"
    android:padding="10dp">

    <!-- App label row -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layout_marginBottom="6dp">

        <ImageView
            android:layout_width="14dp"
            android:layout_height="14dp"
            android:src="@drawable/ic_widget_go"
            android:tint="#EF4444"
            android:contentDescription="CrossSafe" />

        <TextView
            android:layout_marginStart="4dp"
            android:text="CrossSafe"
            android:textColor="#AAFFFFFF"
            android:textSize="10sp"
            android:letterSpacing="0.06" />
    </LinearLayout>

    <!-- Big GO button -->
    <LinearLayout
        android:id="@+id/btnGoFull"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1.5"
        android:orientation="horizontal"
        android:gravity="center"
        android:background="@drawable/widget_go_button_full_bg"
        android:layout_marginBottom="6dp">

        <ImageView
            android:layout_width="22dp"
            android:layout_height="22dp"
            android:src="@drawable/ic_widget_go"
            android:tint="#FFFFFF"
            android:contentDescription="Go" />

        <LinearLayout android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginStart="8dp">

            <TextView
                android:text="START FLASH"
                android:textColor="#FFFFFF"
                android:textSize="14sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/activePresetName"
                android:textColor="#CCFFFFFF"
                android:textSize="10sp" />
        </LinearLayout>
    </LinearLayout>

    <!-- 4 preset chips row -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="horizontal"
        android:layout_marginBottom="6dp">

        <LinearLayout android:id="@+id/chip1" style="@style/WidgetPresetChip">
            <ImageView android:id="@+id/chip1Icon" style="@style/WidgetChipIcon" />
            <TextView android:id="@+id/chip1Label" style="@style/WidgetChipLabel" />
        </LinearLayout>

        <LinearLayout android:id="@+id/chip2" style="@style/WidgetPresetChip">
            <ImageView android:id="@+id/chip2Icon" style="@style/WidgetChipIcon" />
            <TextView android:id="@+id/chip2Label" style="@style/WidgetChipLabel" />
        </LinearLayout>

        <LinearLayout android:id="@+id/chip3" style="@style/WidgetPresetChip">
            <ImageView android:id="@+id/chip3Icon" style="@style/WidgetChipIcon" />
            <TextView android:id="@+id/chip3Label" style="@style/WidgetChipLabel" />
        </LinearLayout>

        <LinearLayout android:id="@+id/chip4" style="@style/WidgetPresetChip">
            <ImageView android:id="@+id/chip4Icon" style="@style/WidgetChipIcon" />
            <TextView android:id="@+id/chip4Label" style="@style/WidgetChipLabel" />
        </LinearLayout>
    </LinearLayout>

    <!-- Torch toggle row -->
    <LinearLayout
        android:id="@+id/torchRow"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="@drawable/widget_preset_chip_bg"
        android:padding="6dp">

        <ImageView
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:src="@drawable/ic_widget_torch"
            android:tint="#FFD700"
            android:contentDescription="Torch" />

        <TextView
            android:layout_marginStart="6dp"
            android:text="Torch"
            android:textColor="#FFFFFF"
            android:textSize="11sp"
            android:layout_weight="1" />

        <TextView
            android:id="@+id/torchStatus"
            android:text="ON"
            android:textColor="#22C55E"
            android:textSize="11sp"
            android:textStyle="bold" />
    </LinearLayout>

</LinearLayout>
```

### Widget Style Resources (res/values/styles.xml)

```xml
<style name="WidgetPresetChip">
    <item name="android:layout_width">0dp</item>
    <item name="android:layout_height">match_parent</item>
    <item name="android:layout_weight">1</item>
    <item name="android:orientation">vertical</item>
    <item name="android:gravity">center</item>
    <item name="android:background">@drawable/widget_preset_chip_bg</item>
    <item name="android:layout_marginEnd">4dp</item>
</style>

<style name="WidgetChipIcon">
    <item name="android:layout_width">18dp</item>
    <item name="android:layout_height">18dp</item>
</style>

<style name="WidgetChipLabel">
    <item name="android:layout_width">wrap_content</item>
    <item name="android:layout_height">wrap_content</item>
    <item name="android:textColor">#FFFFFF</item>
    <item name="android:textSize">9sp</item>
    <item name="android:layout_marginTop">3dp</item>
    <item name="android:maxLines">1</item>
    <item name="android:ellipsize">end</item>
</style>
```

---

## 9. Notification — Foreground Service (all Android versions)

```kotlin
fun buildFlashNotification(context: Context): Notification {
    val channelId = "crosssafe_flash_channel"

    // Create notification channel (Android 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Flash active",
            NotificationManager.IMPORTANCE_LOW   // LOW = no sound, shows in shade
        ).apply {
            description = "Shows while CrossSafe is flashing"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    // Stop action PendingIntent
    val stopIntent = Intent(context, FlashService::class.java).apply {
        action = FlashService.ACTION_STOP
    }
    val stopPending = PendingIntent.getService(
        context, 0, stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Open app PendingIntent (tap notification → open FlashActivity)
    val openIntent = Intent(context, FlashActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val openPending = PendingIntent.getActivity(
        context, 1, openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)       // monochrome icon
        .setContentTitle("CrossSafe is active")
        .setContentText("Screen is flashing — tap STOP to end")
        .setContentIntent(openPending)
        .addAction(R.drawable.ic_widget_go, "STOP", stopPending)
        .setOngoing(true)                               // cannot be swiped away
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // show on lock screen
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .build()
}
```

---

## 10. Additional build.gradle Settings

```kotlin
android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.crosssafe.app"
        minSdk = 26            // Android 8.0 — covers 98%+ of active devices
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true     // enables BuildConfig.VERSION_CODE etc.
        viewBinding = true
    }

    bundle {
        language { enableSplit = true }   // split APKs by language
        density { enableSplit = true }    // split APKs by screen density
        abi { enableSplit = true }        // split APKs by CPU arch
    }
}

dependencies {
    // In-app updates
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Edge-to-edge
    implementation("androidx.activity:activity-ktx:1.8.2")

    // WindowManager (foldables, screen size classes)
    implementation("androidx.window:window:1.2.0")

    // Splashscreen API (Android 12+ native splash)
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

### Splash Screen (Android 12+)

```xml
<!-- res/values/themes.xml — add splash theme -->
<style name="Theme.CrossSafe.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#0F0F0F</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="windowSplashScreenIconBackgroundColor">#EF4444</item>
    <item name="postSplashScreenTheme">@style/Theme.CrossSafe.Dark</item>
</style>
```

```kotlin
// In MainActivity.onCreate() — BEFORE setContentView
installSplashScreen()
```
