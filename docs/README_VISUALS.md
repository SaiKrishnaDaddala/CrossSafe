# CrossSafe — Visual Design Upgrade (Gradients, Animations, Icons)

Everything needed to make the app look polished — animated gradient backgrounds that react to the selected preset, better icons, animated UI elements, and smooth transitions throughout.

---

## 1. Core Concept — Preset-Reactive Background

The home screen background shifts color based on whichever preset is selected.
- Colors are very subtle — low opacity so it does not distract
- A soft animated gradient slowly moves (like aurora / breathing effect)
- When user taps a different preset chip, the background smoothly transitions to that preset's color palette

---

## 2. Animated Gradient Background — MainActivity

### Layout: add a canvas behind everything

```xml
<!-- activity_main.xml — root is a FrameLayout -->
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorBackground">

    <!-- Layer 1: Animated gradient canvas (behind everything) -->
    <com.crosssafe.app.ui.GradientBackgroundView
        android:id="@+id/gradientBg"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Layer 2: Actual content (ScrollView with all UI) -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!-- all your existing layout here -->
    </ScrollView>

</FrameLayout>
```

### GradientBackgroundView.kt

```kotlin
// Location: app/src/main/java/com/crosssafe/app/ui/GradientBackgroundView.kt

class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Two blobs of color that slowly drift around the screen
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Current and target colors (for smooth transition between presets)
    private var color1 = Color.parseColor("#1A0A0A")   // default dark red tint
    private var color2 = Color.parseColor("#0A0A1A")   // default dark blue tint
    private var targetColor1 = color1
    private var targetColor2 = color2

    // Animation tick (0.0 → 1.0, loops)
    private var animTick = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 8000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            animTick = it.animatedValue as Float
            invalidate()
        }
    }

    // Color transition animator
    private var colorAnimator: ValueAnimator? = null

    init {
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Blob 1: top-left area, drifts slowly
        val cx1 = w * (0.2f + 0.15f * sin(animTick * 2 * Math.PI).toFloat())
        val cy1 = h * (0.25f + 0.12f * cos(animTick * 2 * Math.PI).toFloat())

        // Blob 2: bottom-right area, drifts in opposite phase
        val cx2 = w * (0.75f + 0.15f * sin((animTick + 0.5f) * 2 * Math.PI).toFloat())
        val cy2 = h * (0.65f + 0.12f * cos((animTick + 0.5f) * 2 * Math.PI).toFloat())

        // Draw blob 1 (radial gradient)
        val radius1 = w * 0.65f
        val gradient1 = RadialGradient(
            cx1, cy1, radius1,
            intArrayOf(Color.argb(60, Color.red(color1), Color.green(color1), Color.blue(color1)), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient1
        canvas.drawCircle(cx1, cy1, radius1, gradientPaint)

        // Draw blob 2 (radial gradient)
        val radius2 = w * 0.55f
        val gradient2 = RadialGradient(
            cx2, cy2, radius2,
            intArrayOf(Color.argb(45, Color.red(color2), Color.green(color2), Color.blue(color2)), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient2
        canvas.drawCircle(cx2, cy2, radius2, gradientPaint)
    }

    // Call this when user selects a different preset
    fun transitionToColors(newColor1: Int, newColor2: Int) {
        targetColor1 = newColor1
        targetColor2 = newColor2

        colorAnimator?.cancel()
        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                color1 = blendColors(color1, targetColor1, t)
                color2 = blendColors(color2, targetColor2, t)
            }
            start()
        }
    }

    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val invRatio = 1f - ratio
        val r = (Color.red(from) * invRatio + Color.red(to) * ratio).toInt()
        val g = (Color.green(from) * invRatio + Color.green(to) * ratio).toInt()
        val b = (Color.blue(from) * invRatio + Color.blue(to) * ratio).toInt()
        return Color.rgb(r, g, b)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
        colorAnimator?.cancel()
    }
}
```

### Preset → Background Color Mapping

```kotlin
// Location: app/src/main/java/com/crosssafe/app/ui/PresetTheme.kt

object PresetTheme {

    // Returns Pair(blob1Color, blob2Color) for the gradient background
    // Colors are dark tinted versions of the preset colors — subtle, not harsh
    fun getBackgroundColors(presetId: String): Pair<Int, Int> {
        return when (presetId) {
            "police"        -> Pair(Color.parseColor("#1A0515"), Color.parseColor("#05101A")) // dark purple + dark blue
            "ambulance"     -> Pair(Color.parseColor("#1A0505"), Color.parseColor("#1A1005")) // dark red + dark warm
            "fire"          -> Pair(Color.parseColor("#1A0800"), Color.parseColor("#1A0500")) // dark orange + dark red
            "sos"           -> Pair(Color.parseColor("#1A0000"), Color.parseColor("#0D0000")) // deep red
            "hazard"        -> Pair(Color.parseColor("#1A0F00"), Color.parseColor("#150D00")) // dark amber
            "white_only"    -> Pair(Color.parseColor("#111115"), Color.parseColor("#0D0D12")) // cool near-white
            "red_only"      -> Pair(Color.parseColor("#1A0303"), Color.parseColor("#0D0202")) // dark red
            "blue_only"     -> Pair(Color.parseColor("#020A1A"), Color.parseColor("#01061A")) // dark blue
            "amber_only"    -> Pair(Color.parseColor("#1A1000"), Color.parseColor("#150D00")) // dark amber
            "green_only"    -> Pair(Color.parseColor("#001A05"), Color.parseColor("#00150A")) // dark green
            "tricolor"      -> Pair(Color.parseColor("#1A0505"), Color.parseColor("#020A1A")) // dark red + dark blue
            "rainbow"       -> Pair(Color.parseColor("#0A001A"), Color.parseColor("#1A0A00")) // purple + amber
            "heartbeat"     -> Pair(Color.parseColor("#1A0205"), Color.parseColor("#0D0108")) // deep red-pink
            "sos_white"     -> Pair(Color.parseColor("#0F0F0F"), Color.parseColor("#111111")) // near neutral
            "night_red"     -> Pair(Color.parseColor("#120100"), Color.parseColor("#0A0000")) // very dim red
            "night_blue"    -> Pair(Color.parseColor("#01030F"), Color.parseColor("#010209")) // very dim blue
            "night_amber"   -> Pair(Color.parseColor("#0F0800"), Color.parseColor("#0A0500")) // very dim amber
            else            -> Pair(Color.parseColor("#0A0A0A"), Color.parseColor("#0F0F0F")) // neutral dark
        }
    }
}
```

### Wire it up in MainActivity

```kotlin
// When user selects a preset chip:
fun onPresetSelected(preset: Preset) {
    viewModel.selectPreset(preset)

    val (c1, c2) = PresetTheme.getBackgroundColors(preset.id)
    binding.gradientBg.transitionToColors(c1, c2)

    // Update GO button text
    binding.btnGo.text = "Start ${preset.name}"
}
```

---

## 3. GO Button — Visual Upgrade

Replace the plain red circle with a glowing pulsing button.

### Drawable: `res/drawable/bg_go_button.xml`

```xml
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Outer glow ring (slightly larger, more transparent) -->
    <item
        android:left="4dp" android:top="4dp"
        android:right="4dp" android:bottom="4dp">
        <shape android:shape="oval">
            <solid android:color="#33EF4444" />
        </shape>
    </item>

    <!-- Main red circle -->
    <item
        android:left="10dp" android:top="10dp"
        android:right="10dp" android:bottom="10dp">
        <shape android:shape="oval">
            <gradient
                android:type="radial"
                android:gradientRadius="80%"
                android:startColor="#FF2222"
                android:centerColor="#EF4444"
                android:endColor="#CC1111" />
        </shape>
    </item>

</layer-list>
```

### GO Button Pulse Animation

```kotlin
// PulseAnimator.kt — adds a breathing pulse to the GO button

object PulseAnimator {

    fun start(view: View): AnimatorSet {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.06f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.06f, 1f)

        return AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 2000L
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    fun stop(view: View, animatorSet: AnimatorSet) {
        animatorSet.cancel()
        view.scaleX = 1f
        view.scaleY = 1f
    }
}

// In MainActivity:
private var goButtonPulse: AnimatorSet? = null

override fun onResume() {
    goButtonPulse = PulseAnimator.start(binding.btnGo)
}
override fun onPause() {
    goButtonPulse?.let { PulseAnimator.stop(binding.btnGo, it) }
}
```

### GO Button tap effect — ripple + scale down

```kotlin
binding.btnGo.setOnClickListener {
    // Quick scale-down then up (tap feedback)
    it.animate()
        .scaleX(0.93f).scaleY(0.93f)
        .setDuration(80L)
        .withEndAction {
            it.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()

    // Then launch flash
    Handler(Looper.getMainLooper()).postDelayed({ onGoButtonTapped() }, 150L)
}
```

---

## 4. Preset Chips — Visual Upgrade

### Chip Drawables

```xml
<!-- res/drawable/bg_preset_chip.xml (unselected) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#1AFFFFFF" />    <!-- 10% white on dark background -->
    <corners android:radius="14dp" />
    <stroke android:width="0.5dp" android:color="#33FFFFFF" />
</shape>

<!-- res/drawable/bg_preset_chip_selected.xml (selected — glowing border) -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Outer glow -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#33ACCENTCOLOR" />    <!-- replace with preset accent color dynamically -->
            <corners android:radius="16dp" />
        </shape>
    </item>
    <!-- Inner chip -->
    <item android:left="2dp" android:top="2dp" android:right="2dp" android:bottom="2dp">
        <shape android:shape="rectangle">
            <solid android:color="#22000000" />
            <corners android:radius="14dp" />
            <stroke android:width="1.5dp" android:color="#ACCENTCOLOR" />
        </shape>
    </item>
</layer-list>
```

Since selected border color changes per preset, set it programmatically:

```kotlin
// ChipStyleHelper.kt

fun applySelectedStyle(chip: View, accentColor: Int) {
    val gd = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 14.dp
        setStroke(2.dp.toInt(), accentColor)
        setColor(Color.argb(30, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)))
    }
    chip.background = gd
}

fun applyUnselectedStyle(chip: View) {
    val gd = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 14.dp
        setStroke(1, Color.argb(50, 255, 255, 255))
        setColor(Color.argb(25, 255, 255, 255))
    }
    chip.background = gd
}

val Int.dp: Float get() = this * Resources.getSystem().displayMetrics.density
```

### Chip Selection Animation

```kotlin
fun animateChipSelection(chip: View) {
    // Scale up briefly when selected
    chip.animate()
        .scaleX(1.05f).scaleY(1.05f)
        .setDuration(100L)
        .withEndAction {
            chip.animate().scaleX(1f).scaleY(1f).setDuration(100L).start()
        }.start()
}
```

---

## 5. Icons — Material Symbols + Custom Vectors

### Use Material Symbols (Rounded style)

Add to `build.gradle`:
```kotlin
implementation("com.google.android.material:material:1.11.0")
```

And add the font to `res/font/`:

Or use the Google Fonts Adaptive Icons CDN approach — add this to your app's `build.gradle`:
```kotlin
implementation("androidx.core:core-ktx:1.12.0")
```

Then in `res/values/font_provider.xml`:
```xml
<PreloadedFonts xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:name="@font/material_symbols_rounded" />
</PreloadedFonts>
```

### Icon Mapping — Use These for Each Feature

```
Home / GO button     → ic_flash_on (bold lightning bolt)
Torch                → ic_flashlight_on / ic_flashlight_off
Auto-stop timer      → ic_timer
Flash speed          → ic_speed
Settings             → ic_tune (sliders icon, better than gear)
Presets              → ic_style (or ic_palette)
Police preset        → ic_local_police
SOS preset           → ic_sos
Ambulance preset     → ic_emergency
Heartbeat preset     → ic_favorite (heart)
Shake trigger        → ic_vibration
Volume trigger       → ic_volume_down
Widget               → ic_widgets
Back/exit            → ic_arrow_back
More presets         → ic_apps (grid of dots)
Pin/favourite        → ic_push_pin
Custom preset        → ic_edit_note
Brightness           → ic_brightness_high
```

### Custom Vector Drawables — Create These

```xml
<!-- res/drawable/ic_flash_bolt.xml — main app icon and GO button icon -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M13,2L4.5,13.5H11L10,22L19.5,10.5H13L13,2Z" />
</vector>

<!-- res/drawable/ic_preset_police.xml — blue + red shield -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#60A5FA"
        android:pathData="M12,2L4,5.5V11C4,15.42 7.5,19.57 12,21C16.5,19.57 20,15.42 20,11V5.5L12,2Z" />
    <path
        android:fillColor="#EF4444"
        android:pathData="M12,2L12,21C7.5,19.57 4,15.42 4,11V5.5L12,2Z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,7L13.5,10.5H17L14.5,12.5L15.5,16L12,14L8.5,16L9.5,12.5L7,10.5H10.5L12,7Z" />
</vector>
```

---

## 6. FlashActivity Background — Color Wash Effect

Instead of a hard color switch, add a smooth fade between flash colors with a subtle vignette.

```kotlin
// In FlashActivity — enhanced flash rendering

class FlashRenderer(private val rootView: View) {
    private var currentColor = Color.BLACK
    private var targetColor = Color.BLACK
    private var colorAnimator: ValueAnimator? = null

    // Instead of setBackgroundColor() directly, animate between colors
    fun flashToColor(newColor: Int, durationMs: Long = 80L) {
        colorAnimator?.cancel()

        if (newColor == Color.BLACK) {
            // OFF state: instant black (no need to animate)
            rootView.setBackgroundColor(Color.BLACK)
            currentColor = Color.BLACK
            return
        }

        colorAnimator = ValueAnimator.ofArgb(currentColor, newColor).apply {
            duration = durationMs
            addUpdateListener { anim ->
                rootView.setBackgroundColor(anim.animatedValue as Int)
            }
            addListener(onEnd = {
                currentColor = newColor
            })
            start()
        }
    }
}
```

### Vignette Overlay on FlashActivity

Adds a subtle dark edge around the screen so the flash doesn't look flat:

```xml
<!-- In activity_flash.xml — on top of the colored background -->
<View
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/vignette_overlay"
    android:alpha="0.35" />
```

```xml
<!-- res/drawable/vignette_overlay.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:type="radial"
        android:gradientRadius="80%"
        android:startColor="#00000000"
        android:endColor="#AA000000" />
</shape>
```

---

## 7. Onboarding — Gradient Card Backgrounds

Each onboarding card gets a subtle gradient background unique to that card:

```xml
<!-- Card 1 (Epilepsy warning) — amber tint -->
<LinearLayout
    android:background="@drawable/bg_onboard_amber"
    ...>

<!-- Card 2 (How to use) — blue tint -->
<!-- Card 3 (Exit methods) — green tint -->
<!-- Card 4 (Widget tip) — purple tint -->
```

```xml
<!-- res/drawable/bg_onboard_amber.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <color android:color="#0F0F0F" />
    </item>
    <item android:gravity="top|center_horizontal">
        <shape android:shape="oval">
            <solid android:color="#1AF59E0B" />     <!-- 10% amber blob top center -->
            <size android:width="300dp" android:height="300dp" />
        </shape>
    </item>
</layer-list>
```

---

## 8. Settings Screen — Section Header Gradient Lines

Replace plain text section headers with gradient-accented headers:

```kotlin
// In SettingsFragment — custom divider decoration

class GradientDividerDecoration(private val context: Context) : RecyclerView.ItemDecoration() {
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // Draw a thin gradient line under each category header
        // Gradient goes from accent color → transparent (left to right)
    }
}
```

Or simpler — in each `PreferenceCategory`, add a custom layout with a gradient left border:

```xml
<!-- res/layout/preference_category_header.xml -->
<LinearLayout
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="16dp"
    android:paddingBottom="8dp">

    <!-- Gradient accent bar on left -->
    <View
        android:layout_width="3dp"
        android:layout_height="20dp"
        android:background="@drawable/gradient_accent_bar"
        android:layout_marginEnd="10dp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="12sp"
        android:textColor="#9CA3AF"
        android:textAllCaps="true"
        android:letterSpacing="0.1" />
</LinearLayout>
```

```xml
<!-- res/drawable/gradient_accent_bar.xml -->
<shape android:shape="rectangle">
    <gradient
        android:type="linear"
        android:angle="270"
        android:startColor="#EF4444"
        android:endColor="#60A5FA" />
    <corners android:radius="2dp" />
</shape>
```

---

## 9. Widget — Glassmorphism Background

Make widgets look like frosted glass:

```xml
<!-- res/drawable/widget_glass_bg.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Base dark -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#CC0D0D0D" />
            <corners android:radius="20dp" />
        </shape>
    </item>
    <!-- Subtle top highlight line (glass reflection effect) -->
    <item android:bottom="4dp" android:left="8dp" android:right="8dp" android:top="0dp">
        <shape android:shape="rectangle">
            <solid android:color="#00000000" />
            <stroke android:width="0.5dp" android:color="#22FFFFFF" />
            <corners android:radius="20dp" />
        </shape>
    </item>
</layer-list>
```

```xml
<!-- Widget GO button — gradient red -->
<!-- res/drawable/widget_go_gradient.xml -->
<shape android:shape="rectangle">
    <gradient
        android:type="linear"
        android:angle="135"
        android:startColor="#FF4444"
        android:endColor="#CC1111" />
    <corners android:radius="12dp" />
</shape>
```

---

## 10. Transition Animations Between Screens

### MainActivity → FlashActivity

```kotlin
// In MainActivity — override the transition when starting FlashActivity
fun startFlashActivity() {
    val intent = Intent(this, FlashActivity::class.java)
    startActivity(intent)

    // Custom transition: current screen fades out, flash screen zooms in
    overridePendingTransition(R.anim.flash_enter, R.anim.flash_exit)
}
```

```xml
<!-- res/anim/flash_enter.xml — FlashActivity comes in -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <scale
        android:fromXScale="0.92" android:toXScale="1.0"
        android:fromYScale="0.92" android:toYScale="1.0"
        android:pivotX="50%" android:pivotY="50%"
        android:duration="250"
        android:interpolator="@android:interpolator/decelerate_quint" />
    <alpha
        android:fromAlpha="0.0" android:toAlpha="1.0"
        android:duration="200" />
</set>

<!-- res/anim/flash_exit.xml — MainActivity fades out -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:fromAlpha="1.0" android:toAlpha="0.0"
        android:duration="200" />
</set>
```

### FlashActivity → MainActivity (exit)

```kotlin
// In FlashActivity.stopFlashAndExit()
finish()
overridePendingTransition(R.anim.home_enter, R.anim.home_exit)
```

```xml
<!-- res/anim/home_exit.xml — FlashActivity zooms back out -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <scale
        android:fromXScale="1.0" android:toXScale="0.92"
        android:fromYScale="1.0" android:toYScale="0.92"
        android:pivotX="50%" android:pivotY="50%"
        android:duration="200" />
    <alpha
        android:fromAlpha="1.0" android:toAlpha="0.0"
        android:duration="200" />
</set>

<!-- res/anim/home_enter.xml — MainActivity fades back in -->
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:fromAlpha="0.0" android:toAlpha="1.0"
        android:duration="250" />
</set>
```

### Disable all animations if user has Reduce Motion ON

```kotlin
// Check reduce motion setting before applying any animation
fun isReduceMotionEnabled(): Boolean {
    return prefs.getBoolean(PrefKeys.REDUCE_MOTION, false)
}

fun startFlashActivity() {
    startActivity(intent)
    if (!isReduceMotionEnabled()) {
        overridePendingTransition(R.anim.flash_enter, R.anim.flash_exit)
    }
    // If reduce motion: no transition call = default instant switch
}
```

---

## 11. Claude Code Instructions for This File

Tell Claude Code:

```
Read docs/README_VISUALS.md and implement the full visual upgrade for CrossSafe:

1. Create GradientBackgroundView.kt and add it to activity_main.xml
2. Create PresetTheme.kt with all preset-to-background-color mappings
3. Create PulseAnimator.kt and wire it to the GO button
4. Create ChipStyleHelper.kt and apply dynamic chip selection styles
5. Create FlashRenderer.kt and use it in FlashActivity instead of direct setBackgroundColor
6. Create all the drawable XML files: bg_go_button.xml, bg_preset_chip.xml, bg_preset_chip_selected.xml, vignette_overlay.xml, widget_glass_bg.xml, gradient_accent_bar.xml, all bg_onboard_*.xml files
7. Create all the animation XML files in res/anim/: flash_enter.xml, flash_exit.xml, home_enter.xml, home_exit.xml
8. Create all custom vector icon drawables: ic_flash_bolt.xml, ic_preset_police.xml
9. Apply screen transition animations in MainActivity and FlashActivity
10. Respect the reduce_motion preference — skip all animations if it is enabled
```
