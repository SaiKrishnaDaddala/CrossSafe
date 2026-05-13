# CrossSafe — Gestures & Exit Controls

Every way a user can exit the flash screen or control it with gestures.

---

## Exit Methods — All 6

### 1. Tap Anywhere (Primary — most important)

```kotlin
// In FlashActivity — the root view captures all touches
rootView.setOnClickListener {
    stopFlashAndExit()
}

// Make sure nothing blocks touch events
// Set clickable = true on rootView
// No other views should intercept touches during flash
```

### 2. Volume Buttons (works with phone in pocket)

```kotlin
// In FlashActivity
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    return when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN -> {
            stopFlashAndExit()
            true  // consume event, prevent volume change
        }
        else -> super.onKeyDown(keyCode, event)
    }
}
```

> Important: return `true` to consume the event so the volume bar doesn't appear.

### 3. Double Shake (hands-free)

```kotlin
// In FlashActivity — register sensor listener
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private val SHAKE_THRESHOLD = 15f      // acceleration threshold
    private val SHAKE_RESET_TIME = 800L    // ms between shakes to count as double

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

        if (acceleration > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime < SHAKE_RESET_TIME) {
                shakeCount++
                if (shakeCount >= 2) {
                    shakeCount = 0
                    onShake()
                }
            } else {
                shakeCount = 1
            }
            lastShakeTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

// Register in onResume, unregister in onPause
private fun registerShakeDetector() {
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    shakeDetector = ShakeDetector { stopFlashAndExit() }
    sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
}
```

### 4. Swipe Up (Android navigation gesture)

```kotlin
// Swipe up exits naturally if using the standard Android gesture nav
// For immersive mode (where nav bar is hidden), add gesture detector:

val gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent,
        velocityX: Float, velocityY: Float
    ): Boolean {
        val dy = (e1?.y ?: 0f) - e2.y
        if (dy > 200 && Math.abs(velocityY) > 300) {
            // Upward fling detected
            stopFlashAndExit()
            return true
        }
        return false
    }
})

rootView.setOnTouchListener { _, event ->
    gestureDetector.onTouchEvent(event)
    false  // return false so tap listener also fires
}
```

### 5. Auto-Stop Timer

```kotlin
// Starts when flash starts
// Configured by user in settings (default 90 seconds)
// Shows countdown on flash screen

private var countdownJob: Job? = null

fun startAutoStop(durationMs: Long) {
    if (durationMs <= 0L) return
    countdownJob = lifecycleScope.launch {
        var remaining = durationMs
        while (remaining > 0) {
            delay(1000L)
            remaining -= 1000L
            updateCountdownDisplay(remaining)
            if (remaining <= 10_000L) {
                // Last 10 seconds: turn timer text red + pulse animation
                showWarningCountdown()
            }
        }
        stopFlashAndExit()
    }
}
```

### 6. Back Button / Home Button

```kotlin
// Back button → stops flash and exits to MainActivity
override fun onBackPressed() {
    stopFlashAndExit()
}

// Home button → flash keeps running as foreground service
// Notification appears with STOP button
// If user taps STOP in notification → sends ACTION_STOP to FlashService
override fun onPause() {
    super.onPause()
    if (isFinishing) {
        flashEngine.stop()
    } else {
        // App backgrounded — start foreground service to keep flash alive
        startFlashService()
    }
}

override fun onResume() {
    super.onResume()
    // App came back to foreground — stop service, resume in-activity flash
    stopFlashService()
}
```

---

## Hint Overlay (first 3 seconds)

```kotlin
// Show "Tap to stop" at top of screen, fade after 3 seconds

private fun showHintOverlay() {
    binding.hintBar.visibility = View.VISIBLE
    binding.hintBar.alpha = 1f

    Handler(Looper.getMainLooper()).postDelayed({
        binding.hintBar.animate()
            .alpha(0f)
            .setDuration(800L)
            .withEndAction { binding.hintBar.visibility = View.GONE }
            .start()
    }, 3000L)
}
```

Hint bar layout (`view_hint_bar.xml`):
```xml
<LinearLayout
    android:id="@+id/hintBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#55000000"
    android:padding="12dp"
    android:orientation="horizontal">

    <TextView
        android:text="Tap anywhere to stop"
        android:textColor="#FFFFFF"
        android:textSize="14sp"
        android:layout_weight="1"/>

    <TextView
        android:id="@+id/presetNameText"
        android:textColor="#CCFFFFFF"
        android:textSize="13sp"/>

</LinearLayout>
```

---

## In-Flash Gestures (while flash is active)

### Brightness — Two-Finger Swipe

```kotlin
// Two-finger vertical swipe = brightness control
// One-finger is used for tap-to-stop, so use two fingers for brightness

private var currentBrightness = 1.0f

val multiTouchListener = object : View.OnTouchListener {
    private var startY1 = 0f
    private var startY2 = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.pointerCount == 2) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    startY1 = event.getY(0)
                    startY2 = event.getY(1)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = ((startY1 - event.getY(0)) + (startY2 - event.getY(1))) / 2f
                    val delta = dy / 1000f   // scale gesture to brightness change
                    currentBrightness = (currentBrightness + delta).coerceIn(0.1f, 1.0f)
                    flashEngine.setBrightness(currentBrightness)
                    showBrightnessBar(currentBrightness)
                }
            }
            return true  // consume 2-finger events
        }
        return false  // pass 1-finger events to tap handler
    }
}
```

### Show Brightness Bar (right edge of screen)

```kotlin
// Appears when user adjusts brightness, fades after 1.5 seconds

fun showBrightnessBar(level: Float) {
    binding.brightnessBar.visibility = View.VISIBLE
    binding.brightnessProgress.progress = (level * 100).toInt()
    binding.brightnessPercent.text = "${(level * 100).toInt()}%"

    brightnessHideHandler.removeCallbacksAndMessages(null)
    brightnessHideHandler.postDelayed({
        binding.brightnessBar.animate().alpha(0f).setDuration(500L)
            .withEndAction { binding.brightnessBar.visibility = View.GONE }
            .start()
    }, 1500L)
}
```

### Preset Cycle — Left/Right Swipe

```kotlin
// Swipe left → next preset, swipe right → previous preset
// Switches preset WITHOUT stopping the flash

val gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent,
        velocityX: Float, velocityY: Float
    ): Boolean {
        val dx = (e1?.x ?: 0f) - e2.x
        val dy = (e1?.y ?: 0f) - e2.y

        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 150) {
            // Horizontal swipe
            if (dx > 0) {
                flashEngine.cycleToNextPreset()
            } else {
                flashEngine.cycleToPreviousPreset()
            }
            showPresetNameBriefly()
            return true
        }
        return false
    }
})
```

### Show Preset Name (brief flash when cycling)

```kotlin
fun showPresetNameBriefly() {
    val name = flashEngine.getCurrentPresetName()
    binding.presetNameToast.text = name
    binding.presetNameToast.visibility = View.VISIBLE
    binding.presetNameToast.alpha = 1f
    binding.presetNameToast.animate()
        .alpha(0f)
        .setStartDelay(1200L)
        .setDuration(600L)
        .withEndAction { binding.presetNameToast.visibility = View.GONE }
        .start()
}
```

---

## stopFlashAndExit() — The Master Stop Function

All 6 exit methods call this one function.

```kotlin
private fun stopFlashAndExit() {
    flashEngine.stop()           // stops color loop, restores brightness, releases WakeLock
    torchManager.setEnabled(false)
    cancelAutoStop()
    unregisterShakeDetector()
    vibrateOnce(50L)             // short haptic to confirm exit
    finish()                     // close FlashActivity, return to MainActivity
}

private fun vibrateOnce(ms: Long) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
    }
}
```

---

## Shake-to-Start (from home screen)

Optional feature — user enables in Settings.

```kotlin
// In MainActivity — shake starts flash immediately

private var shakeToStartEnabled = false  // read from prefs

override fun onResume() {
    super.onResume()
    shakeToStartEnabled = prefs.getBoolean("shake_to_start", false)
    if (shakeToStartEnabled) {
        shakeDetector = ShakeDetector {
            // Vibrate to confirm
            vibrateOnce(100L)
            // Launch flash with current active preset
            startFlashActivity()
        }
        registerShakeDetector()
    }
}

override fun onPause() {
    super.onPause()
    unregisterShakeDetector()
}
```

---

## Volume Trigger (from home screen)

Optional — hold volume down 2 seconds to start flash.

```kotlin
// In MainActivity
private var volumeHoldStartTime = 0L
private val HOLD_DURATION_MS = 2000L

override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        if (volumeHoldStartTime == 0L) {
            volumeHoldStartTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - volumeHoldStartTime >= HOLD_DURATION_MS) {
            startFlashActivity()
            volumeHoldStartTime = 0L
            return true
        }
    }
    return super.onKeyDown(keyCode, event)
}

override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        volumeHoldStartTime = 0L  // reset on release
    }
    return super.onKeyUp(keyCode, event)
}
```
