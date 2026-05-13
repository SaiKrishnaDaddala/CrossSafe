package com.crosssafe.app

import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibrationEffect
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.crosssafe.app.databinding.ActivityMainBinding
import com.crosssafe.app.gesture.ShakeDetector
import com.crosssafe.app.model.Preset
import com.crosssafe.app.model.PrefKeys
import com.crosssafe.app.ui.ChipStyleHelper
import com.crosssafe.app.ui.PresetTheme
import com.crosssafe.app.ui.PulseAnimator
import com.crosssafe.app.util.ChangelogManager
import com.crosssafe.app.util.PermissionManager
import com.crosssafe.app.util.ThemeManager
import com.crosssafe.app.util.UpdateManager
import com.crosssafe.app.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private lateinit var updateManager: UpdateManager
    private lateinit var changelogManager: ChangelogManager
    private lateinit var sensorManager: SensorManager
    private var shakeDetector: ShakeDetector? = null
    private var shakeToStartEnabled = false
    private var volumeHoldStartTime = 0L
    private val HOLD_DURATION_MS = 2000L
    private val prefs by lazy { getSharedPreferences("crosssafe_prefs", MODE_PRIVATE) }
    private var pulseAnimator: AnimatorSet? = null
    private var goButtonPulse: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!prefs.getBoolean(PrefKeys.ONBOARDING_COMPLETE, false)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permissionManager = PermissionManager(this)
        updateManager = UpdateManager(this)
        changelogManager = ChangelogManager(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        setupToolbar()
        setupPresetChips()
        setupGoButton()
        setupQuickControls()
        observeViewModel()

        permissionManager.requestNotificationIfNeeded {}
        updateManager.checkForUpdate()
        changelogManager.showChangelogIfUpdated(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPinnedPresets()
        viewModel.refreshActivePreset()
        updateManager.onResume()
        shakeToStartEnabled = prefs.getBoolean(PrefKeys.SHAKE_TO_START, false)
        if (shakeToStartEnabled) registerShakeDetector()

        if (!isReduceMotionEnabled()) {
            goButtonPulse = PulseAnimator.start(binding.btnGo)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterShakeDetector()
        goButtonPulse?.let { PulseAnimator.stop(binding.btnGo, it) }
        goButtonPulse = null
        pulseAnimator?.cancel()
        pulseAnimator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        pulseAnimator?.cancel()
        if (::updateManager.isInitialized) {
            updateManager.onDestroy()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val volumeTriggerEnabled = prefs.getBoolean(PrefKeys.VOLUME_TRIGGER, false)
        if (volumeTriggerEnabled && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) volumeHoldStartTime = 0L
        return super.onKeyUp(keyCode, event)
    }

    private fun setupToolbar() {
        setupThemeToggle()
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnInfo.setOnClickListener {
            showInfoDialog()
        }
    }

    private fun setupThemeToggle() {
        updateThemeIcon()
        binding.btnTheme.setOnClickListener {
            val next = when (ThemeManager.getCurrent(this)) {
                ThemeManager.THEME_SYSTEM -> ThemeManager.THEME_DARK
                ThemeManager.THEME_DARK   -> ThemeManager.THEME_LIGHT
                else                      -> ThemeManager.THEME_SYSTEM
            }
            ThemeManager.save(this, next)
            ThemeManager.apply(this)
            recreate()
        }
    }

    private fun updateThemeIcon() {
        val iconRes = when (ThemeManager.getCurrent(this)) {
            ThemeManager.THEME_DARK  -> R.drawable.ic_dark_mode
            ThemeManager.THEME_LIGHT -> R.drawable.ic_light_mode
            else                     -> R.drawable.ic_brightness_auto
        }
        binding.btnTheme.setImageResource(iconRes)
    }

    private fun setupPresetChips() {
        val chipBindings = listOf(binding.chip1, binding.chip2, binding.chip3, binding.chip4)
        viewModel.pinnedPresets.observe(this) { presets ->
            chipBindings.forEachIndexed { index, chip ->
                if (index < presets.size) {
                    val preset = presets[index]
                    chip.root.visibility = View.VISIBLE
                    chip.presetName.text = preset.name
                    setChipDots(chip, preset)
                    chip.root.setOnClickListener {
                        if (!isReduceMotionEnabled()) ChipStyleHelper.animateChipSelection(chip.root)
                        viewModel.selectPreset(preset)
                    }
                    chip.root.setOnLongClickListener {
                        showChipContextMenu(preset, index)
                        true
                    }
                } else {
                    chip.root.visibility = View.INVISIBLE
                }
            }
            updateChipSelection()
        }
        binding.btnMorePresets.setOnClickListener { showPresetsBottomSheet() }
    }

    private fun setChipDots(chip: com.crosssafe.app.databinding.ItemPresetChipBinding, preset: Preset) {
        val color1 = preset.colors.getOrElse(0) { Color.WHITE }
        chip.dot1.backgroundTintList = ColorStateList.valueOf(color1)

        if (preset.colors.size > 1) {
            chip.dot2.visibility = View.VISIBLE
            chip.dot2.backgroundTintList = ColorStateList.valueOf(preset.colors[1])
        } else {
            chip.dot2.visibility = View.GONE
        }
    }

    private fun updateChipSelection() {
        val chipBindings = listOf(binding.chip1, binding.chip2, binding.chip3, binding.chip4)
        val activeId = viewModel.activePreset.value?.id
        val pinned = viewModel.pinnedPresets.value ?: emptyList()

        // Stop any existing pulse animation
        pulseAnimator?.cancel()
        pulseAnimator = null

        chipBindings.forEachIndexed { index, chip ->
            if (index < pinned.size) {
                val isSelected = pinned[index].id == activeId
                chip.root.isSelected = isSelected
                chip.pulseIcon.visibility = if (isSelected) View.VISIBLE else View.GONE
                chip.dropdownIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

                if (isSelected) {
                    ChipStyleHelper.applySelectedStyle(chip.root, pinned[index].chipColor)
                    // Start pulse animation on selected chip
                    if (!isReduceMotionEnabled()) {
                        pulseAnimator = PulseAnimator.start(chip.root)
                    }
                } else {
                    ChipStyleHelper.applyUnselectedStyle(chip.root)
                }
            }
        }
    }

    private fun setupGoButton() {
        binding.btnGo.setOnClickListener {
            vibrateOnce(30L)
            if (isReduceMotionEnabled()) {
                onGoButtonTapped()
            } else {
                it.animate()
                    .scaleX(0.93f).scaleY(0.93f)
                    .setDuration(80L)
                    .withEndAction {
                        it.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                    }.start()
                Handler(Looper.getMainLooper()).postDelayed({ onGoButtonTapped() }, 150L)
            }
        }

        viewModel.activePreset.observe(this) { preset ->
            val buttonText = "${preset.name.uppercase()}\n${preset.name} Flash"
            binding.btnGo.text = buttonText
            updateChipSelection()
            val (c1, c2) = PresetTheme.getBackgroundColors(preset.id)
            binding.gradientBg.transitionToColors(c1, c2)
        }
    }

    private fun setupQuickControls() {
        viewModel.torchEnabled.observe(this) { on ->
            binding.torchSwitch.isChecked = on
            binding.torchStatusText.text = if (on) "ON" else "OFF"
        }
        binding.torchSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTorch(isChecked)
        }
        binding.torchRow.setOnClickListener { binding.torchSwitch.toggle() }

        viewModel.autoStopMs.observe(this) { ms ->
            binding.autoStopValue.text = formatAutoStop(ms)
        }
        binding.autoStopRow.setOnClickListener { showAutoStopDialog() }

        viewModel.flashSpeed.observe(this) { speed ->
            binding.speedValue.text = formatSpeed(speed)
        }
        binding.speedRow.setOnClickListener { showSpeedDialog() }
    }

    private fun observeViewModel() {
        viewModel.pinnedPresets.observe(this) { setupPresetChips() }
    }

    private fun onGoButtonTapped() {
        val torchOn = viewModel.torchEnabled.value == true
        if (torchOn) {
            if (!permissionManager.isTorchAvailable()) {
                viewModel.setTorch(false)
                startFlashActivity()
            } else {
                permissionManager.requestCameraIfNeeded(
                    onGranted = { startFlashActivity() },
                    onDenied = { showContinueWithoutTorchDialog() }
                )
            }
        } else {
            startFlashActivity()
        }
    }

    private fun showContinueWithoutTorchDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Torch not available")
            .setMessage("CrossSafe will flash the screen only, without the camera torch. Do you want to continue?")
            .setPositiveButton("Continue") { _, _ -> startFlashActivity() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startFlashActivity() {
        val config = viewModel.buildFlashConfig()
        val intent = Intent(this, FlashActivity::class.java).apply {
            putExtra("preset_id", config.presetId)
            putExtra("interval_ms", config.intervalMs)
            putExtra("torch_enabled", config.torchEnabled)
            putExtra("auto_stop_ms", config.autoStopMs)
            putIntegerArrayListExtra("colors", ArrayList(config.colors))
            putExtra("pattern_type", config.patternType.name)
            putExtra("preset_name", config.presetName)
        }
        startActivity(intent)
    }

    private fun showAutoStopDialog() {
        val labels = arrayOf("Off (no limit)", "30 seconds", "60 seconds", "90 seconds", "2 minutes", "3 minutes")
        val values = longArrayOf(0L, 30_000L, 60_000L, 90_000L, 120_000L, 180_000L)
        val current = viewModel.autoStopMs.value ?: 90_000L
        val checked = values.indexOfFirst { it == current }.coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle("Auto-stop timer")
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                viewModel.setAutoStop(values[which])
                dialog.dismiss()
            }.show()
    }

    private fun showSpeedDialog() {
        val labels = arrayOf("Pulse (very slow)", "Slow", "Medium", "Fast", "Rapid (strobe)")
        val values = arrayOf("PULSE", "SLOW", "MEDIUM", "FAST", "RAPID")
        val current = viewModel.flashSpeed.value ?: "MEDIUM"
        val checked = values.indexOfFirst { it == current }.coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle("Flash speed")
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                viewModel.setFlashSpeed(values[which])
                dialog.dismiss()
            }.show()
    }

    private fun showPresetsBottomSheet() {
        val sheet = com.crosssafe.app.ui.PresetsBottomSheet.newInstance()
        sheet.setOnPresetSelected { preset ->
            viewModel.selectPreset(preset)
        }
        sheet.show(supportFragmentManager, "presets_sheet")
    }

    private fun showChipContextMenu(preset: Preset, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(preset.name)
            .setItems(arrayOf("Remove from home", "Set as active")) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.getRepository().unpinPreset(preset.id)
                        viewModel.refreshPinnedPresets()
                    }
                    1 -> viewModel.selectPreset(preset)
                }
            }.show()
    }

    private fun showInfoDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("CrossSafe")
            .setMessage("A free, offline app that flashes your phone screen to make you visible to drivers while crossing the road at night.\n\nVersion ${BuildConfig.VERSION_NAME}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun registerShakeDetector() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            shakeDetector = ShakeDetector {
                vibrateOnce(100L)
                startFlashActivity()
            }
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

    private fun isReduceMotionEnabled(): Boolean = prefs.getBoolean(PrefKeys.REDUCE_MOTION, false)

    private fun formatAutoStop(ms: Long): String = when (ms) {
        0L -> "Off"
        30_000L -> "30s"
        60_000L -> "60s"
        90_000L -> "90s"
        120_000L -> "2 min"
        180_000L -> "3 min"
        else -> "${ms / 1000}s"
    }

    private fun formatSpeed(speed: String): String = when (speed) {
        "PULSE" -> "Pulse"
        "SLOW" -> "Slow"
        "FAST" -> "Fast"
        "RAPID" -> "Rapid"
        else -> "Medium"
    }
}
