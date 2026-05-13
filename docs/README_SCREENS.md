# CrossSafe — Screens & UI Layout

## Overview — 3 Screens Only

```
SplashActivity → OnboardingActivity (first launch only)
                        ↓
              MainActivity (Home)
                        ↓
              FlashActivity (full screen flash)
                        ↓ (back)
              SettingsActivity
```

---

## Screen 1 — MainActivity (Home)

### Layout: `activity_main.xml`

```
┌─────────────────────────────┐
│  CrossSafe          ⚙  ℹ   │  ← toolbar, settings + info icons top right
├─────────────────────────────┤
│                             │
│  ┌─────┐ ┌─────┐ ┌─────┐   │
│  │ 🔵🔴│ │ 🔴  │ │ ⚪  │   │  ← 4 preset chips (2×2 grid)
│  │Police│ │ Red │ │White│   │     selected one has colored glow border
│  └─────┘ └─────┘ └─────┘   │
│           ┌─────┐           │
│           │ 🟡  │           │
│           │Amber│           │
│           └─────┘           │
│                             │
│        + More presets       │  ← tap opens PresetsBottomSheet
│                             │
│  ┌───────────────────────┐  │
│  │                       │  │
│  │   ●  START POLICE     │  │  ← BIG GO BUTTON (round, red, 140dp diameter)
│  │                       │  │     label shows active preset name
│  └───────────────────────┘  │
│                             │
│  🔦 Torch  ─────────── ON  │  ← torch toggle row
│  ⏱ Auto-stop ───────── 90s │  ← timer quick-pick row
│  ⚡ Speed  ──────── Medium │  ← speed quick-pick row
│                             │
└─────────────────────────────┘
```

### GO Button spec

- Shape: circle, 140dp diameter
- Background: `#EF4444` (red) with a subtle dark red ring border
- Text: "START [PRESET NAME]" in white, 18sp, bold
- Icon: small filled circle (●) left of text, same as original design
- Ripple effect on tap
- Haptic feedback (light vibration) on tap
- On tap: launches FlashActivity with selected preset

### Preset chips (home page, 4 visible)

- Grid: 2 columns × 2 rows
- Each chip: rounded rectangle, 56dp tall
- Unselected: dark background with colored text
- Selected: same background + 2dp colored border (glow color matches preset color) + small checkmark icon top-right
- Long-press chip → shows popup: "Remove from home" / "Configure"
- The 4 shown chips are the user's pinned favourites (saved in SharedPreferences)

### Quick controls (bottom 3 rows)

Each row is a horizontal row with label left, value/toggle right:

**Torch row**
- Toggle switch (Material Switch)
- Default: ON
- Saved to SharedPreferences

**Auto-stop row**
- Tap opens a small bottom dialog with options: Off / 30s / 60s / 90s / 120s / 3min
- Default: 90s

**Speed row**
- Tap opens dialog: Slow / Medium / Fast / Rapid
- Maps to: 1000ms / 600ms / 300ms / 150ms interval

---

## Screen 2 — FlashActivity (full screen flash)

### Layout: `activity_flash.xml`

```
┌─────────────────────────────┐
│ Tap to stop    [PRESET NAME]│  ← hint bar, fades after 3 sec
│─────────────────────────────│
│                             │
│                             │
│       [FULL SCREEN]         │  ← entire screen is the flash color
│       FLASHING COLOR        │     background color changes every interval
│                             │
│         ⏱ 01:28            │  ← countdown timer, large, semi-transparent
│                             │
│                             │
└─────────────────────────────┘
```

### FlashActivity behaviour

- **Immersive full screen** — hide status bar, navigation bar
- `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` — screen never sleeps
- `WindowManager.LayoutParams.FLAG_FULLSCREEN`
- Brightness: force to 1.0f (100%) via `WindowManager.LayoutParams`
- Restore brightness to original value on exit
- Background color switches between preset colors at the configured interval
- If torch is enabled: camera torch fires in sync with the flash

### Hint overlay (top of screen)

- Shows for first 3 seconds: "Tap anywhere to stop"
- Also shows preset name in small text top-right
- Both fade out using `AlphaAnimation` after 3 seconds
- If user has never used the app before (first flash): hint stays 5 seconds

### Countdown timer display

- Large text (48sp), white, centered lower third of screen
- Semi-transparent (50% alpha) so it doesn't reduce flash visibility
- Counts down from configured auto-stop time
- If auto-stop is OFF: shows elapsed time instead (00:00 counting up)
- When 10 seconds remain: timer turns red and pulses

### Brightness control overlay (appears during gesture)

- When user does 2-finger swipe: a vertical brightness bar appears on right edge
- Bar shows current brightness level (0–100%)
- Disappears after 1.5 seconds of no gesture

---

## Screen 3 — SettingsActivity

### Layout: `activity_settings.xml`

Uses `PreferenceFragmentCompat` with standard Android preference groups.

```
Settings
├── Presets
│   ├── Manage pinned presets (drag to reorder)
│   ├── Default preset on open
│   └── Create custom preset
├── Triggers
│   ├── Shake to start (toggle + sensitivity slider)
│   ├── Volume button trigger (toggle)
│   └── Quick Settings tile (instructions)
├── Flash behaviour
│   ├── Default brightness (50–100% slider)
│   ├── Auto-stop timer (list)
│   ├── Flash speed (list)
│   └── Torch on by default (toggle)
├── Sound
│   ├── Audio alert (none/beep/horn/siren)
│   └── Audio volume (slider)
├── Accessibility
│   ├── Text size (normal/large/extra-large)
│   ├── High contrast mode (toggle)
│   └── Reduce motion (toggle)
├── Language
│   └── App language (list: EN/HI/TA/TE/KN/MR/BN/GU)
└── Help & About
    ├── How to use (replay onboarding)
    ├── Epilepsy warning (view again)
    ├── Privacy policy
    └── App version
```

---

## Screen 4 — OnboardingActivity

Covered fully in `README_ONBOARDING.md`

---

## Screen 5 — PresetsBottomSheet

Opens from "More presets" on home screen.

```
┌─────────────────────────────┐
│  ━━━━━  (drag handle)       │
│  All Presets          🔍    │  ← search box
│                             │
│  [Emergency] [Single] [Multi] [Pattern] [Night] [Custom]
│  ← scrollable category tabs →
│                             │
│  ┌────────┐ ┌────────┐      │
│  │ 🔵🔴  │ │  🔴    │      │  ← preset grid, 2 columns
│  │ Police │ │  Red   │      │
│  │ ⭐ Pin │ │ ⭐ Pin │      │  ← pin button on each card
│  └────────┘ └────────┘      │
│      ...more presets...     │
└─────────────────────────────┘
```

- Tap any preset → selects it as active, closes sheet, home screen updates
- Long-press preset card → "Pin to home" / "Set as default" / "Preview"
- Star/pin icon: filled = pinned to home, outline = not pinned
- Max 4 pinned to home at once — if already 4, ask user which to replace
- Search filters presets by name in real time

---

## Colors & Typography

```kotlin
// Main colors (define in colors.xml)
colorPrimary      = #EF4444   // Red (GO button)
colorBackground   = #0F0F0F   // Near black (home screen bg)
colorSurface      = #1A1A1A   // Dark cards
colorText         = #FFFFFF   // White text
colorTextSecond   = #9CA3AF   // Muted gray text
colorAccent       = #60A5FA   // Blue accent

// Text sizes (define in dimens.xml)
textSizeHuge      = 48sp   // Flash countdown
textSizeLarge     = 20sp   // GO button
textSizeBody      = 16sp   // Normal body
textSizeSmall     = 13sp   // Chips, labels
textSizeTiny      = 11sp   // Hints, captions
```

---

## AndroidManifest.xml — Required Entries

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<uses-feature android:name="android.hardware.camera.flash" android:required="false" />

<activity android:name=".MainActivity"
    android:theme="@style/Theme.CrossSafe.Dark"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity android:name=".FlashActivity"
    android:theme="@style/Theme.CrossSafe.Flash"
    android:screenOrientation="portrait"
    android:showWhenLocked="true"
    android:turnScreenOn="true" />

<activity android:name=".OnboardingActivity"
    android:theme="@style/Theme.CrossSafe.Dark" />

<activity android:name=".SettingsActivity"
    android:theme="@style/Theme.CrossSafe.Dark" />

<service android:name=".FlashService" />
```
