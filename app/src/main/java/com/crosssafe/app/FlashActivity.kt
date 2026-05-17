package com.crosssafe.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.crosssafe.app.databinding.ActivityFlashBinding
import com.crosssafe.app.engine.FlashEngine
import com.crosssafe.app.engine.TorchManager
import com.crosssafe.app.gesture.ShakeDetector
import com.crosssafe.app.model.FlashConfig
import com.crosssafe.app.model.PatternType
import com.crosssafe.app.model.PrefKeys
import com.crosssafe.app.service.FlashService
import com.crosssafe.app.ui.FlashRenderer
import com.crosssafe.app.viewmodel.FlashViewModel
import java.util.Locale

class FlashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashBinding
    private val viewModel: FlashViewModel by viewModels()
    private lateinit var flashEngine: FlashEngine
    private lateinit var torchManager: TorchManager
    private lateinit var sensorManager: SensorManager
    private var shakeDetector: ShakeDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var brightnessHideHandler = Handler(Looper.getMainLooper())
    private var currentBrightness = 1.0f
    private var hasExited = false
    private var stopOverlayVisible = false
    private val overlayDismissHandler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupShowOnLockScreen()
        binding = ActivityFlashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setDecorFitsSystemWindows(false)

        val config = readConfigFromIntent()
        viewModel.config = config
        viewModel.currentPresetName.value = config.presetName

        torchManager = TorchManager(this)
        flashEngine = FlashEngine(this, window, binding.flashRoot, config, torchManager)
        if (!prefs.getBoolean(PrefKeys.REDUCE_MOTION, false)) {
            flashEngine.setFlashRenderer(FlashRenderer(binding.flashRoot))
        }

        setupGestures()
        observeViewModel()
        showHintOverlay(config.presetName)

        binding.btnStopConfirm.setOnClickListener { stopFlashAndExit() }
        binding.btnStopCancel.setOnClickListener { hideStopOverlay() }

        flashEngine.start()
        viewModel.startCountdown(config.autoStopMs)

        viewModel.shouldExit.observe(this) { should ->
            if (should == true && !hasExited) stopFlashAndExit()
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
        stopFlashService()
        registerShakeDetector()
    }

    override fun onPause() {
        super.onPause()
        unregisterShakeDetector()
        if (!isFinishing && !hasExited) {
            startFlashService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // CRITICAL: Always ensure torch is turned off when activity is destroyed
        // This prevents the torch from getting stuck on
        flashEngine.stop()
        torchManager.setEnabled(false)
        torchManager.cleanup()
        stopFlashService()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                stopFlashAndExit()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        stopFlashAndExit()
    }

    private fun readConfigFromIntent(): FlashConfig {
        val colors = intent.getIntegerArrayListExtra("colors") ?: arrayListOf(-65536, -16711936)
        return FlashConfig(
            presetId = intent.getStringExtra("preset_id") ?: "police",
            colors = colors,
            intervalMs = intent.getLongExtra("interval_ms", 300L),
            torchEnabled = intent.getBooleanExtra("torch_enabled", true),
            autoStopMs = intent.getLongExtra("auto_stop_ms", 90_000L),
            patternType = try {
                PatternType.valueOf(intent.getStringExtra("pattern_type") ?: "ALTERNATING")
            } catch (e: Exception) { PatternType.ALTERNATING },
            presetName = intent.getStringExtra("preset_name") ?: "Police"
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (stopOverlayVisible) hideStopOverlay() else showStopOverlay()
                return true
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                val dx = (e1?.x ?: 0f) - e2.x
                val dy = (e1?.y ?: 0f) - e2.y

                return when {
                    // swipe DOWN (finger moves top→bottom, dy < 0, velocityY > 0)
                    Math.abs(dy) > Math.abs(dx) && dy < -200 && velocityY > 300 -> {
                        stopFlashAndExit()
                        true
                    }
                    Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 150 -> {
                        if (dx > 0) flashEngine.cycleToNextPreset()
                        else flashEngine.cycleToPreviousPreset()
                        showPresetNameBriefly()
                        true
                    }
                    else -> false
                }
            }
        })

        val multiTouchListener = object : View.OnTouchListener {
            private var startY1 = 0f
            private var startY2 = 0f

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.pointerCount == 2) {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            startY1 = event.getY(0)
                            startY2 = event.getY(1)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dy = ((startY1 - event.getY(0)) + (startY2 - event.getY(1))) / 2f
                            val delta = dy / 1000f
                            currentBrightness = (currentBrightness + delta).coerceIn(0.1f, 1.0f)
                            flashEngine.setBrightness(currentBrightness)
                            showBrightnessBar(currentBrightness)
                        }
                    }
                    return true
                }
                gestureDetector?.onTouchEvent(event)
                return false
            }
        }

        binding.flashRoot.setOnTouchListener(multiTouchListener)
    }

    private fun observeViewModel() {
        viewModel.remainingMs.observe(this) { ms ->
            if (ms != null && viewModel.config?.autoStopMs ?: 0 > 0) {
                updateCountdownDisplay(ms)
            }
        }
        viewModel.elapsedMs.observe(this) { ms ->
            if (viewModel.config?.autoStopMs ?: 1L <= 0L) {
                binding.countdownText.text = formatTime(ms)
            }
        }
        viewModel.isWarning.observe(this) { warning ->
            if (warning) {
                binding.countdownText.setTextColor(getColor(R.color.colorDanger))
                startPulseAnimation()
            }
        }
    }

    private fun updateCountdownDisplay(remainingMs: Long) {
        binding.countdownText.text = formatTime(remainingMs)
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.US, "%02d:%02d", min, sec)
    }

    private fun startPulseAnimation() {
        binding.countdownText.animate()
            .scaleX(1.2f).scaleY(1.2f)
            .setDuration(500L)
            .withEndAction {
                binding.countdownText.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(500L)
                    .withEndAction { if (!hasExited) startPulseAnimation() }
                    .start()
            }.start()
    }

    private fun showHintOverlay(presetName: String) {
        binding.hintBar.visibility = View.VISIBLE
        binding.hintBar.alpha = 1f
        binding.presetNameText.text = presetName

        Handler(Looper.getMainLooper()).postDelayed({
            binding.hintBar.animate()
                .alpha(0f)
                .setDuration(800L)
                .withEndAction { binding.hintBar.visibility = View.GONE }
                .start()
        }, 3000L)
    }

    private fun showStopOverlay() {
        stopOverlayVisible = true
        binding.stopOverlay.visibility = View.VISIBLE
        binding.stopOverlay.animate()
            .translationY(0f)
            .setDuration(280L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        overlayDismissHandler.removeCallbacksAndMessages(null)
        overlayDismissHandler.postDelayed({ if (!hasExited) hideStopOverlay() }, 5000L)
    }

    private fun hideStopOverlay() {
        if (!stopOverlayVisible) return
        stopOverlayVisible = false
        overlayDismissHandler.removeCallbacksAndMessages(null)
        val slideDown = binding.stopOverlay.height.toFloat().coerceAtLeast(400f)
        binding.stopOverlay.animate()
            .translationY(slideDown)
            .setDuration(220L)
            .withEndAction { binding.stopOverlay.visibility = View.GONE }
            .start()
    }

    private fun showBrightnessBar(level: Float) {
        binding.brightnessBar.visibility = View.VISIBLE
        binding.brightnessBar.alpha = 1f
        binding.brightnessProgress.progress = (level * 100).toInt()
        binding.brightnessPercent.text = "${(level * 100).toInt()}%"

        brightnessHideHandler.removeCallbacksAndMessages(null)
        brightnessHideHandler.postDelayed({
            binding.brightnessBar.animate().alpha(0f).setDuration(500L)
                .withEndAction { binding.brightnessBar.visibility = View.GONE }
                .start()
        }, 1500L)
    }

    private fun showPresetNameBriefly() {
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

    fun stopFlashAndExit() {
        if (hasExited) return
        hasExited = true
        overlayDismissHandler.removeCallbacksAndMessages(null)
        flashEngine.stop()
        torchManager.setEnabled(false)
        viewModel.cancelCountdown()
        unregisterShakeDetector()
        vibrateOnce(50L)
        finish()
        if (!prefs.getBoolean(PrefKeys.REDUCE_MOTION, false)) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.home_enter, R.anim.home_exit)
        }
    }

    private fun registerShakeDetector() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            shakeDetector = ShakeDetector { stopFlashAndExit() }
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterShakeDetector() {
        shakeDetector?.let { sensorManager.unregisterListener(it) }
        shakeDetector = null
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

    private fun enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
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

    private fun setupShowOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startFlashService() {
        val intent = Intent(this, FlashService::class.java).apply {
            action = FlashService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFlashService() {
        val intent = Intent(this, FlashService::class.java).apply {
            action = FlashService.ACTION_STOP
        }
        startService(intent)
    }
}
