# CrossSafe — Visual Fixes & Theme System

Fixes the icon pack, adds dark/light/system theme support, and upgrades the UI to match the design preview.

---

## 1. Replace Icons — Use Material Design 3 Icons (Official Google Pack)

Remove any existing icon setup. Use the official Material Symbols font from Google.

### Step 1 — Add dependency to build.gradle
```kotlin
dependencies {
    // Material Components (already there — bump to latest)
    implementation("com.google.android.material:material:1.12.0")
}
```

### Step 2 — Download the icon font

Go to: https://fonts.google.com/icons
- Style: Rounded
- Download the font file: `MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf`
- Place it in: `app/src/main/res/font/material_symbols_rounded.ttf`

### Step 3 — Create font XML
```xml
<!-- res/font/material_symbols.xml -->
<font-family xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <font
        app:fontStyle="normal"
        app:fontWeight="400"
        app:font="@font/material_symbols_rounded" />
</font-family>
```

### Step 4 — Create a helper for icon TextViews
```xml
<!-- In res/values/styles.xml — add this style -->
<style name="MaterialIcon">
    <item name="android:fontFamily">@font/material_symbols_rounded</item>
    <item name="android:textSize">24sp</item>
    <item name="android:includeFontPadding">false</item>
</style>
```

### Step 5 — Use in layouts like this
```xml
<TextView
    style="@style/MaterialIcon"
    android:text="&#xe518;"    <!-- flashlight_on codepoint -->
    android:textColor="@color/iconColor"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

---

## 2. Icon Codepoints — Use These Exact Ones

All from Material Symbols Rounded. Copy the codepoint exactly into android:text.

```
flashlight_on      &#xe518;   → Torch row icon
flashlight_off     &#xe519;   → Torch OFF state
timer              &#xe425;   → Auto-stop row
speed              &#xe9fb;   → Speed row
tune               &#xe429;   → Settings (top bar)
info               &#xe88e;   → Info button (top bar)
bolt               &#xe69b;   → GO button icon
shield             &#xe9d0;   → Police preset
circle             &#xef4a;   → Red / single color presets
light_mode         &#xe518;   → White preset
warning_amber      &#xe002;   → Amber / hazard preset
sos                &#xebf5;   → SOS preset
favorite           &#xe87d;   → Heartbeat preset
palette            &#xe40a;   → Custom preset
push_pin           &#xe10d;   → Pin to home
star               &#xe838;   → Favourite
check_circle       &#xe86c;   → Selected preset checkmark
add_circle         &#xe147;   → Add preset
widgets            &#xe1bd;   → Widget tip icon
vibration          &#xe62d;   → Shake trigger
volume_down        &#xe04d;   → Volume trigger
brightness_high    &#xe1ac;   → Brightness
close              &#xe5cd;   → Close / stop
arrow_back         &#xe5c4;   → Back
emergency          &#xe1f2;   → Emergency presets
local_fire_dept    &#xef55;   → Fire preset
```

---

## 3. Theme System — Dark / Light / System Default

### ThemeManager.kt
```kotlin
// Location: app/src/main/java/com/crosssafe/app/util/ThemeManager.kt

object ThemeManager {

    const val THEME_DARK    = "dark"
    const val THEME_LIGHT   = "light"
    const val THEME_SYSTEM  = "system"   // follows device setting

    fun apply(context: Context) {
        val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
        val theme = prefs.getString("app_theme", THEME_SYSTEM) ?: THEME_SYSTEM

        when (theme) {
            THEME_DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun save(context: Context, theme: String) {
        context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
            .edit().putString("app_theme", theme).apply()
    }

    fun getCurrent(context: Context): String {
        return context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
            .getString("app_theme", THEME_SYSTEM) ?: THEME_SYSTEM
    }
}
```

### Call ThemeManager in Application class
```kotlin
// Location: app/src/main/java/com/crosssafe/app/CrossSafeApp.kt

class CrossSafeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.apply(this)   // apply theme before any activity opens
    }
}
```

```xml
<!-- In AndroidManifest.xml — register the Application class -->
<application
    android:name=".CrossSafeApp"
    ...>
```

### Theme Colors — Both Modes

```xml
<!-- res/values/colors.xml (LIGHT MODE) -->
<resources>
    <color name="colorBackground">#F5F5F7</color>
    <color name="colorSurface">#FFFFFF</color>
    <color name="colorSurfaceVariant">#EFEFEF</color>
    <color name="colorText">#0A0A0A</color>
    <color name="colorTextSecondary">#6B7280</color>
    <color name="colorPrimary">#EF4444</color>
    <color name="colorPrimaryDark">#B91C1C</color>
    <color name="colorChipBackground">#EEEEEE</color>
    <color name="colorChipBorder">#DDDDDD</color>
    <color name="colorControlRow">#F0F0F0</color>
    <color name="colorControlBorder">#E0E0E0</color>
    <color name="iconColor">#555555</color>
    <color name="iconColorAccent">#EF4444</color>
</resources>

<!-- res/values-night/colors.xml (DARK MODE) -->
<resources>
    <color name="colorBackground">#0D0D0D</color>
    <color name="colorSurface">#1A1A1A</color>
    <color name="colorSurfaceVariant">#242424</color>
    <color name="colorText">#FFFFFF</color>
    <color name="colorTextSecondary">#9CA3AF</color>
    <color name="colorPrimary">#EF4444</color>
    <color name="colorPrimaryDark">#B91C1C</color>
    <color name="colorChipBackground">#1E1E1E</color>
    <color name="colorChipBorder">#333333</color>
    <color name="colorControlRow">#1A1A1A</color>
    <color name="colorControlBorder">#2A2A2A</color>
    <color name="iconColor">#9CA3AF</color>
    <color name="iconColorAccent">#EF4444</color>
</resources>
```

### Themes.xml — Full Setup

```xml
<!-- res/values/themes.xml -->
<resources>

    <!-- Base theme — shared by both modes -->
    <style name="Theme.CrossSafe.Base" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorOnPrimary">#FFFFFF</item>
        <item name="android:windowBackground">@color/colorBackground</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLightStatusBar">@bool/isLightMode</item>
        <item name="android:windowLightNavigationBar">@bool/isLightMode</item>
        <item name="android:textColorPrimary">@color/colorText</item>
        <item name="android:textColorSecondary">@color/colorTextSecondary</item>
    </style>

    <!-- Main app theme -->
    <style name="Theme.CrossSafe" parent="Theme.CrossSafe.Base" />

    <!-- Flash activity — always dark regardless of system theme -->
    <style name="Theme.CrossSafe.Flash" parent="Theme.Material3.NoActionBar">
        <item name="android:windowBackground">#000000</item>
        <item name="android:statusBarColor">#000000</item>
        <item name="android:navigationBarColor">#000000</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowLightNavigationBar">false</item>
    </style>

    <!-- Splash screen -->
    <style name="Theme.CrossSafe.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">#0D0D0D</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
        <item name="windowSplashScreenIconBackgroundColor">#EF4444</item>
        <item name="postSplashScreenTheme">@style/Theme.CrossSafe</item>
    </style>

</resources>

<!-- res/values/bools.xml -->
<resources>
    <bool name="isLightMode">false</bool>   <!-- default dark -->
</resources>

<!-- res/values-night/bools.xml — not needed since night = dark always -->
```

---

## 4. Theme Picker UI — In Settings

Add a theme selector that looks like a card with 3 options:

```kotlin
// In SettingsFragment — handle the theme preference

fun setupThemePicker() {
    findPreference<ListPreference>("app_theme")?.apply {
        entries = arrayOf("System default", "Dark", "Light")
        entryValues = arrayOf("system", "dark", "light")
        value = ThemeManager.getCurrent(requireContext())
        summary = entry

        setOnPreferenceChangeListener { _, newValue ->
            ThemeManager.save(requireContext(), newValue as String)
            // Recreate all activities to apply theme
            requireActivity().recreate()
            true
        }
    }
}
```

Add to `res/xml/preferences.xml` under the Accessibility section:
```xml
<ListPreference
    app:key="app_theme"
    app:title="App theme"
    app:icon="@drawable/ic_theme"
    app:entries="@array/theme_labels"
    app:entryValues="@array/theme_values"
    app:summary="%s"
    app:defaultValue="system" />
```

Add to `res/values/arrays.xml`:
```xml
<string-array name="theme_labels">
    <item>System default</item>
    <item>Dark</item>
    <item>Light</item>
</string-array>
<string-array name="theme_values">
    <item>system</item>
    <item>dark</item>
    <item>light</item>
</string-array>
```

---

## 5. Chip UI Fix — Match the Preview Exactly

The current chips look too plain. Here is the exact fix:

### chip_item.xml layout
```xml
<!-- res/layout/item_preset_chip.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingStart="14dp"
    android:paddingEnd="14dp"
    android:background="@drawable/bg_preset_chip"
    android:clickable="true"
    android:focusable="true">

    <!-- Color dots (1 or 2) -->
    <LinearLayout
        android:id="@+id/dotsContainer"
        android:orientation="horizontal"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:layout_marginEnd="10dp">

        <View
            android:id="@+id/dot1"
            android:layout_width="14dp"
            android:layout_height="14dp"
            android:background="@drawable/bg_color_dot" />

        <View
            android:id="@+id/dot2"
            android:layout_width="14dp"
            android:layout_height="14dp"
            android:layout_marginStart="4dp"
            android:background="@drawable/bg_color_dot"
            android:visibility="gone" />
    </LinearLayout>

    <!-- Preset name -->
    <TextView
        android:id="@+id/presetName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:textColor="@color/colorText"
        android:textSize="14sp"
        android:fontFamily="sans-serif-medium" />

    <!-- Selected checkmark -->
    <TextView
        android:id="@+id/checkIcon"
        style="@style/MaterialIcon"
        android:text="&#xe86c;"
        android:textSize="18sp"
        android:textColor="@color/colorPrimary"
        android:visibility="gone" />

</LinearLayout>
```

### Color dot drawable
```xml
<!-- res/drawable/bg_color_dot.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#FFFFFF" />
    <size android:width="14dp" android:height="14dp" />
</shape>
```

### Selected chip drawable
```xml
<!-- res/drawable/bg_preset_chip_selected.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/colorChipBackground" />
    <corners android:radius="14dp" />
    <stroke android:width="2dp" android:color="@color/colorPrimary" />
</shape>

<!-- res/drawable/bg_preset_chip.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/colorChipBackground" />
    <corners android:radius="14dp" />
    <stroke android:width="0.5dp" android:color="@color/colorChipBorder" />
</shape>
```

---

## 6. Control Rows Fix

The torch / auto-stop / speed rows need a cleaner look:

```xml
<!-- res/layout/view_control_row.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="52dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:background="@color/colorBackground">

    <!-- Left: icon -->
    <TextView
        android:id="@+id/rowIcon"
        style="@style/MaterialIcon"
        android:textSize="20sp"
        android:textColor="@color/iconColor"
        android:layout_width="28dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="12dp" />

    <!-- Center: label -->
    <TextView
        android:id="@+id/rowLabel"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:textColor="@color/colorText"
        android:textSize="15sp" />

    <!-- Right: value / toggle -->
    <TextView
        android:id="@+id/rowValue"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@color/colorTextSecondary"
        android:textSize="14sp"
        android:layout_marginEnd="8dp" />

    <com.google.android.material.materialswitch.MaterialSwitch
        android:id="@+id/rowToggle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone" />

    <!-- Divider at bottom — add programmatically or via RecyclerView decorator -->
</LinearLayout>
```

---

## 7. GO Button — Exact Drawable

```xml
<!-- res/drawable/bg_go_button.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Outermost glow ring -->
    <item>
        <shape android:shape="oval">
            <solid android:color="#1AEF4444" />
        </shape>
    </item>

    <!-- Middle ring -->
    <item android:left="6dp" android:top="6dp" android:right="6dp" android:bottom="6dp">
        <shape android:shape="oval">
            <solid android:color="#33EF4444" />
        </shape>
    </item>

    <!-- Main button -->
    <item android:left="14dp" android:top="14dp" android:right="14dp" android:bottom="14dp">
        <shape android:shape="oval">
            <gradient
                android:type="radial"
                android:gradientRadius="70%"
                android:startColor="#FF5555"
                android:endColor="#CC1111" />
        </shape>
    </item>

</layer-list>
```

---

## 8. Top Bar Fix

Replace the current settings gear with proper icons:

```xml
<!-- In activity_main.xml toolbar area -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingStart="20dp"
    android:paddingEnd="12dp">

    <!-- App name with dot accent -->
    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="CrossSafe"
        android:textColor="@color/colorText"
        android:textSize="22sp"
        android:fontFamily="sans-serif-medium" />

    <!-- Theme toggle (sun/moon icon) -->
    <ImageButton
        android:id="@+id/btnTheme"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="Toggle theme" />

    <!-- Settings -->
    <ImageButton
        android:id="@+id/btnSettings"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@drawable/ic_settings"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:tint="@color/iconColor"
        android:contentDescription="Settings" />

    <!-- Info -->
    <ImageButton
        android:id="@+id/btnInfo"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:src="@drawable/ic_info"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:tint="@color/iconColor"
        android:contentDescription="Info" />

</LinearLayout>
```

### Theme toggle button logic
```kotlin
// In MainActivity
fun setupThemeToggle() {
    updateThemeIcon()
    binding.btnTheme.setOnClickListener {
        val current = ThemeManager.getCurrent(this)
        val next = when (current) {
            ThemeManager.THEME_SYSTEM -> ThemeManager.THEME_DARK
            ThemeManager.THEME_DARK   -> ThemeManager.THEME_LIGHT
            ThemeManager.THEME_LIGHT  -> ThemeManager.THEME_SYSTEM
            else -> ThemeManager.THEME_SYSTEM
        }
        ThemeManager.save(this, next)
        ThemeManager.apply(this)
        recreate()
    }
}

fun updateThemeIcon() {
    val iconRes = when (ThemeManager.getCurrent(this)) {
        ThemeManager.THEME_DARK   -> R.drawable.ic_dark_mode
        ThemeManager.THEME_LIGHT  -> R.drawable.ic_light_mode
        else                      -> R.drawable.ic_brightness_auto
    }
    binding.btnTheme.setImageResource(iconRes)
}
```

---

## 9. Vector Icons to Create

Create these in `res/drawable/`:

```xml
<!-- ic_settings.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/iconColor"
        android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"/>
</vector>

<!-- ic_info.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/iconColor"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10s10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2V7h2v2z"/>
</vector>

<!-- ic_dark_mode.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/iconColor"
        android:pathData="M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9s9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36c-0.98,1.37 -2.58,2.26 -4.4,2.26c-2.98,0 -5.4,-2.42 -5.4,-5.4c0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3L12,3z"/>
</vector>

<!-- ic_light_mode.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/iconColor"
        android:pathData="M6.76,4.84l-1.8,-1.79 -1.41,1.41 1.79,1.79 1.42,-1.41zM4,10.5H1v2h3v-2zM13,0.55h-2V3.5h2V0.55zM20.45,3.46l-1.41,-1.41 -1.79,1.79 1.41,1.41 1.79,-1.79zM17.24,18.16l1.79,1.8 1.41,-1.41 -1.8,-1.79 -1.4,1.4zM20,10.5v2h3v-2h-3zM12,5.5c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6zM11,22.45h2V19.5h-2v2.95zM3.55,18.54l1.41,1.41 1.79,-1.8 -1.41,-1.41 -1.79,1.8z"/>
</vector>

<!-- ic_brightness_auto.xml (for system/auto mode) -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="@color/iconColor"
        android:pathData="M10.85,12.65h2.3L12,9l-1.15,3.65zM20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69L23.31,12 20,8.69zM14.3,16l-0.7,-2h-3.2l-0.7,2H7.8L11,7h2l3.2,9h-1.9z"/>
</vector>
```

---

## 10. Claude Code — Single Instruction

Add this file to your `docs/` folder, then tell Claude Code:

```
Read docs/README_VISUAL_FIXES.md and apply every change described:

1. Set up Material Symbols Rounded font in res/font/ and create the MaterialIcon style
2. Replace ALL existing icons in layouts with Material Symbols codepoints from section 2
3. Create ThemeManager.kt and CrossSafeApp.kt for dark/light/system theme support
4. Create res/values/colors.xml and res/values-night/colors.xml with the exact colors in section 3
5. Update themes.xml with Theme.CrossSafe.Base, Theme.CrossSafe, Theme.CrossSafe.Flash, and Theme.CrossSafe.Splash
6. Add theme picker ListPreference to preferences.xml and arrays.xml
7. Update item_preset_chip.xml to use the new layout from section 5
8. Create all drawable XML files: bg_preset_chip.xml, bg_preset_chip_selected.xml, bg_color_dot.xml, bg_go_button.xml
9. Update activity_main.xml top bar with theme toggle button, settings icon, info icon
10. Create all vector icon drawables: ic_settings.xml, ic_info.xml, ic_dark_mode.xml, ic_light_mode.xml, ic_brightness_auto.xml
11. Wire theme toggle button in MainActivity using ThemeManager
12. Register CrossSafeApp in AndroidManifest.xml
13. Build and fix all errors
```
