# CrossSafe — Flash Engine

The flash engine is the heart of the app. It controls screen color, brightness, torch, and timing.

---

## Core Class: `FlashEngine.kt`

```kotlin
// Location: app/src/main/java/com/crosssafe/app/engine/FlashEngine.kt

class FlashEngine(
    private val context: Context,
    private val window: Window,
    private val rootView: View,
    private val config: FlashConfig
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var currentColorIndex = 0
    private var torchManager: TorchManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    fun start() { ... }
    fun stop() { ... }
    fun updateConfig(config: FlashConfig) { ... }
    fun setBrightness(level: Float) { ... }  // 0.0f to 1.0f
    fun cycleToPreviousPreset() { ... }
    fun cycleToNextPreset() { ... }
    fun increaseSpeed() { ... }
    fun decreaseSpeed() { ... }
}
```

---

## Data Class: `FlashConfig`

```kotlin
// Location: app/src/main/java/com/crosssafe/app/model/FlashConfig.kt

data class FlashConfig(
    val colors: List<Int>,          // list of Color ints to cycle through
    val intervalMs: Long,           // milliseconds between color switches
    val torchEnabled: Boolean,      // fire camera torch in sync
    val torchSyncMode: TorchSync,   // SYNC_WITH_FLASH or ALWAYS_ON or ALWAYS_OFF
    val brightness: Float = 1.0f,   // screen brightness (always 1.0f = 100%)
    val autoStopMs: Long = 90_000L, // 0 = no auto-stop
    val patternType: PatternType = PatternType.ALTERNATING
)

enum class TorchSync { SYNC_WITH_FLASH, ALWAYS_ON, ALWAYS_OFF }

enum class PatternType {
    ALTERNATING,   // color A → color B → color A → ...
    SEQUENTIAL,    // color A → B → C → D → A → ...
    HEARTBEAT,     // flash twice quick, pause, flash twice quick
    SOS,           // morse code S-O-S pattern (3 short, 3 long, 3 short)
    DOUBLE_FLASH,  // two quick flashes then gap
    TRIPLE_FLASH   // three quick flashes then gap
}
```

---

## Flash Loop Logic

```kotlin
fun start() {
    if (isRunning) return
    isRunning = true
    acquireWakeLock()
    saveBrightness()
    setBrightness(1.0f)           // force 100% brightness
    scheduleNextFlash()
}

private fun scheduleNextFlash() {
    if (!isRunning) return

    val color = getNextColor()    // depends on PatternType
    rootView.setBackgroundColor(color)

    if (config.torchEnabled && config.torchSyncMode == TorchSync.SYNC_WITH_FLASH) {
        torchManager?.setEnabled(color != Color.BLACK)
    }

    handler.postDelayed({ scheduleNextFlash() }, config.intervalMs)
}

fun stop() {
    isRunning = false
    handler.removeCallbacksAndMessages(null)
    rootView.setBackgroundColor(Color.BLACK)
    torchManager?.setEnabled(false)
    restoreBrightness()
    releaseWakeLock()
}
```

---

## Brightness Control

```kotlin
// Save original brightness before flash starts
private var originalBrightness: Float = -1f

private fun saveBrightness() {
    originalBrightness = window.attributes.screenBrightness
    // -1f means "use system setting" — save the system setting too
    if (originalBrightness < 0) {
        originalBrightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, 128
        ) / 255f
    }
}

fun setBrightness(level: Float) {
    val params = window.attributes
    params.screenBrightness = level.coerceIn(0.1f, 1.0f)
    window.attributes = params
}

private fun restoreBrightness() {
    setBrightness(originalBrightness)
}
```

---

## WakeLock

```kotlin
private fun acquireWakeLock() {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = pm.newWakeLock(
        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
        "CrossSafe:FlashWakeLock"
    )
    wakeLock?.acquire(30 * 60 * 1000L) // max 30 min safety timeout
}

private fun releaseWakeLock() {
    if (wakeLock?.isHeld == true) {
        wakeLock?.release()
    }
    wakeLock = null
}
```

---

## Pattern Types — Implementation

### ALTERNATING (default — e.g. Red ↔ Blue)
```kotlin
// colors = [RED, BLUE]
// RED → BLUE → RED → BLUE → ...
currentColorIndex = (currentColorIndex + 1) % colors.size
```

### HEARTBEAT (two quick pulses then pause)
```kotlin
// sequence: ON, OFF, ON, OFF(long), ON, OFF, ON, OFF(long)...
val beatPattern = listOf(
    Pair(color, 120L),
    Pair(Color.BLACK, 80L),
    Pair(color, 120L),
    Pair(Color.BLACK, 600L)
)
```

### SOS (Morse: · · · — — — · · ·)
```kotlin
val dot = 200L
val dash = 600L
val gap = 200L
val letterGap = 800L
val sosPattern = listOf(
    // S: dot dot dot
    Pair(color, dot), Pair(Color.BLACK, gap),
    Pair(color, dot), Pair(Color.BLACK, gap),
    Pair(color, dot), Pair(Color.BLACK, letterGap),
    // O: dash dash dash
    Pair(color, dash), Pair(Color.BLACK, gap),
    Pair(color, dash), Pair(Color.BLACK, gap),
    Pair(color, dash), Pair(Color.BLACK, letterGap),
    // S: dot dot dot
    Pair(color, dot), Pair(Color.BLACK, gap),
    Pair(color, dot), Pair(Color.BLACK, gap),
    Pair(color, dot), Pair(Color.BLACK, 2000L)  // long pause before repeat
)
```

---

## TorchManager Class

```kotlin
// Location: app/src/main/java/com/crosssafe/app/engine/TorchManager.kt

class TorchManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        // Find the camera with flash
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    fun setEnabled(on: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, on)
        } catch (e: CameraAccessException) {
            // Device doesn't support torch — silently ignore
        }
    }

    fun isAvailable(): Boolean = cameraId != null
}
```

---

## Speed Presets (interval in milliseconds)

```kotlin
enum class FlashSpeed(val intervalMs: Long, val label: String) {
    SLOW(1200L, "Slow"),
    MEDIUM(600L, "Medium"),
    FAST(300L, "Fast"),
    RAPID(150L, "Rapid"),
    PULSE(2000L, "Pulse")   // for soft glow/night presets
}
```

---

## Auto-Stop Timer

```kotlin
// In FlashActivity or FlashViewModel
private var autoStopJob: Job? = null

fun startAutoStop(durationMs: Long, onStop: () -> Unit) {
    if (durationMs <= 0) return
    autoStopJob = CoroutineScope(Dispatchers.Main).launch {
        delay(durationMs)
        onStop()
    }
}

fun cancelAutoStop() {
    autoStopJob?.cancel()
}

// Countdown ticker (updates UI every second)
fun startCountdown(totalMs: Long, onTick: (remainingMs: Long) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        var remaining = totalMs
        while (remaining > 0) {
            delay(1000L)
            remaining -= 1000L
            onTick(remaining)
        }
    }
}
```

---

## FlashService (Foreground Service)

Required so flash keeps working if user presses Home button.

```kotlin
// Location: app/src/main/java/com/crosssafe/app/service/FlashService.kt

class FlashService : Service() {
    // Shows a persistent notification with a STOP button
    // If user presses Home: flash continues + notification shows "CrossSafe active — tap to stop"
    // Notification has a PendingIntent that stops the flash and kills the service

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startFlash()
            ACTION_STOP  -> stopFlash()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        // "CrossSafe is active" notification
        // Action button: STOP (calls ACTION_STOP)
        // Tapping notification: opens FlashActivity
    }

    companion object {
        const val ACTION_START = "com.crosssafe.ACTION_START"
        const val ACTION_STOP  = "com.crosssafe.ACTION_STOP"
    }
}
```

---

## Color Definitions for Flash Colors

```kotlin
// Location: app/src/main/java/com/crosssafe/app/model/FlashColors.kt

object FlashColors {
    val RED     = Color.parseColor("#FF0000")
    val BLUE    = Color.parseColor("#0066FF")
    val WHITE   = Color.parseColor("#FFFFFF")
    val AMBER   = Color.parseColor("#FF8C00")
    val GREEN   = Color.parseColor("#00CC44")
    val YELLOW  = Color.parseColor("#FFEE00")
    val CYAN    = Color.parseColor("#00CCFF")
    val MAGENTA = Color.parseColor("#FF00CC")
    val ORANGE  = Color.parseColor("#FF5500")
    val PURPLE  = Color.parseColor("#9900FF")
    val BLACK   = Color.BLACK  // used as OFF state
}
```
