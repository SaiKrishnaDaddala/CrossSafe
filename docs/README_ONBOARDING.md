# CrossSafe — Onboarding Flow

First-launch experience. 4 swipeable cards. Shown only once (unless replayed from Settings).

---

## When to Show Onboarding

```kotlin
// In SplashActivity or MainActivity onCreate()
val prefs = getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)
val hasSeenOnboarding = prefs.getBoolean("onboarding_complete", false)

if (!hasSeenOnboarding) {
    startActivity(Intent(this, OnboardingActivity::class.java))
    finish()
}
```

---

## OnboardingActivity Structure

```kotlin
// Location: app/src/main/java/com/crosssafe/app/OnboardingActivity.kt

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView

    // 4 fragments: EpilepsyFragment, HowToUseFragment, ExitMethodsFragment, WidgetTipFragment
    // Card 1 (Epilepsy) has NO skip button and NEXT is disabled until user taps "I Understand"
    // Cards 2–4 have a Skip button top-right
}
```

---

## Card 1 — Epilepsy Warning (CANNOT be skipped)

### Layout
```
┌──────────────────────────────┐
│                              │
│         ⚠️                   │  ← large warning icon (60dp), amber color
│                              │
│   Important warning          │  ← 22sp, bold
│                              │
│  This app flashes bright     │
│  colors rapidly.             │  ← 16sp body text
│                              │
│  People with epilepsy or     │
│  photosensitive conditions   │
│  should NOT use this app     │
│  without consulting a        │
│  doctor first.               │
│                              │
│  If you feel unwell during   │
│  use, stop immediately.      │
│                              │
│                              │
│  [ I understand — Continue ] │  ← button, disabled until 2 seconds pass
│                              │
│    (no skip button)          │
└──────────────────────────────┘
```

### Code
```kotlin
class EpilepsyFragment : Fragment() {
    private var canProceed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnContinue = view.findViewById<Button>(R.id.btnContinue)
        btnContinue.isEnabled = false
        btnContinue.text = "Please read (2s)..."

        // Enable after 2 seconds so user actually reads it
        Handler(Looper.getMainLooper()).postDelayed({
            btnContinue.isEnabled = true
            btnContinue.text = "I understand — Continue"
        }, 2000L)

        btnContinue.setOnClickListener {
            (activity as? OnboardingActivity)?.goToNextCard()
        }
    }
}
```

---

## Card 2 — How to Use the App

### Layout
```
┌──────────────────────────────┐
│                      [Skip]  │
│                              │
│  How CrossSafe works         │  ← 20sp bold
│                              │
│  ① Select a preset           │
│     Tap any colored chip     │
│                              │
│  ② Tap GO                    │
│     The big red button       │
│                              │
│  ③ Place phone facing        │
│     traffic                  │
│     Screen faces drivers     │
│                              │
│  ④ Cross safely              │
│     Phone flashes until      │
│     timer stops              │
│                              │
│  ● ○ ○ ○        [ Next → ]  │
└──────────────────────────────┘
```

### Code
```kotlin
class HowToUseFragment : Fragment() {
    // Shows 4 numbered steps with icons
    // Each step has: number badge, short title, one-line description
    // Steps animate in with a slight slide-up delay (50ms between each)

    private fun animateStepsIn() {
        val steps = listOf(
            binding.step1, binding.step2,
            binding.step3, binding.step4
        )
        steps.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 120L)
                .setDuration(300L)
                .start()
        }
    }
}
```

---

## Card 3 — How to Exit Flash Screen

This card is critical — every exit method shown before the user ever flashes.

### Layout
```
┌──────────────────────────────┐
│                      [Skip]  │
│                              │
│  How to stop flashing        │  ← 20sp bold
│                              │
│  ── Tap anywhere ──          │  ← most prominent, large icon
│     Touch any part of        │
│     the screen to stop       │
│                              │
│  📳 Shake twice              │
│  🔊 Press volume button      │
│  ↑  Swipe up                 │
│  ⏱  Auto-stops after timer   │
│  ← Back button               │
│                              │
│  ● ● ○ ○        [ Next → ]  │
└──────────────────────────────┘
```

### Code
```kotlin
class ExitMethodsFragment : Fragment() {
    // Each exit method shown as a row:
    // [icon]  [title]  [description]
    // Tap anywhere is shown larger / more prominent than others
    // Small animation: each row slides in from left with staggered delay
}
```

---

## Card 4 — Widget Tip

### Layout
```
┌──────────────────────────────┐
│                      [Skip]  │
│                              │
│  Quick tip! 💡               │  ← 20sp bold
│                              │
│  Add CrossSafe to your       │
│  home screen for instant     │
│  one-tap access.             │
│                              │
│  [  Widget preview image  ]  │  ← illustration of 1×1 widget
│                              │
│  Long-press your home        │
│  screen → Widgets →          │
│  CrossSafe                   │
│                              │
│  [+ Add widget now]          │  ← opens Android widget picker if possible
│                              │
│  ● ● ● ○     [ Get Started ] │  ← last card, "Get Started" instead of "Next"
└──────────────────────────────┘
```

### Code
```kotlin
class WidgetTipFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<Button>(R.id.btnAddWidget).setOnClickListener {
            // Try to open widget picker (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val appWidgetManager = AppWidgetManager.getInstance(requireContext())
                val provider = ComponentName(requireContext(), CrossSafeWidgetProvider::class.java)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    appWidgetManager.requestPinAppWidget(provider, null, null)
                }
            }
        }

        view.findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            (activity as? OnboardingActivity)?.completeOnboarding()
        }
    }
}
```

---

## Completing Onboarding

```kotlin
// In OnboardingActivity
fun completeOnboarding() {
    // Mark as complete
    prefs.edit().putBoolean("onboarding_complete", true).apply()
    // Go to main screen
    startActivity(Intent(this, MainActivity::class.java))
    finish()
}
```

---

## Replaying Onboarding (from Settings)

```kotlin
// In SettingsActivity, under Help & About
"How to use" preference click:
    → Set onboarding_complete = false (optional, or just launch OnboardingActivity directly)
    → startActivity(Intent(this, OnboardingActivity::class.java))
```

---

## Dot Indicator

```xml
<!-- At the bottom of each card fragment (or in activity layout) -->
<LinearLayout
    android:id="@+id/dotsContainer"
    android:orientation="horizontal"
    android:gravity="center">
    <!-- 4 dot views, filled/outline depending on current page -->
</LinearLayout>
```

```kotlin
// Update dots when page changes
viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
        updateDots(position)
        // Hide skip button on card 1
        btnSkip.visibility = if (position == 0) View.GONE else View.VISIBLE
        // Change Next to Get Started on last card
        btnNext.text = if (position == 3) "Get Started" else "Next →"
    }
})
```

---

## Styling — Onboarding Theme

```xml
<!-- Dark, clean, friendly -->
<style name="Theme.CrossSafe.Onboarding" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="android:windowBackground">#0F0F0F</item>
    <item name="colorPrimary">#EF4444</item>
    <item name="colorOnPrimary">#FFFFFF</item>
    <item name="android:textColorPrimary">#FFFFFF</item>
    <item name="android:textColorSecondary">#9CA3AF</item>
</style>
```
