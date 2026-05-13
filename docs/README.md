# CrossSafe — Pedestrian Safety App

A free, offline Android app that flashes your phone screen in bright colors to make you visible to drivers while crossing the road at night.

---

## 📁 README Files — Read in This Order

| File | What it covers |
|---|---|
| `README.md` | This file — project overview and structure |
| `README_SCREENS.md` | All screens, layouts, and UI behaviour |
| `README_FLASH_ENGINE.md` | Flash logic, brightness, torch, color system |
| `README_PRESETS.md` | All 25+ presets, categories, home pinning |
| `README_GESTURES_EXIT.md` | All exit methods and in-flash gestures |
| `README_ONBOARDING.md` | First-launch flow, all 4 onboarding cards |
| `README_WIDGETS.md` | Home screen widgets (1×1, 2×1, 2×2) |
| `README_SETTINGS.md` | Every settings option and behaviour |
| `README_ARCHITECTURE.md` | Folder structure, classes, data flow |
| `README_PERMISSIONS_COMPAT.md` | All permissions, runtime requests, in-app updates, screen sizes, Android skin fixes, widget UI |

---

## 🎯 What the App Does

- User opens app → selects a preset (or uses last-used one) → taps the big **GO** button
- Phone screen flashes in bright colors at 100% brightness
- Camera torch (flashlight) also fires in sync
- User places phone on the road-facing side while crossing
- Drivers see the flashing light and slow down
- Flash stops automatically after a timer, or user exits in any of 6 ways

---

## 🧱 Tech Stack

| Item | Choice |
|---|---|
| Language | Kotlin |
| Min SDK | Android 8.0 (API 26) |
| Target SDK | Android 14 (API 34) |
| UI | XML layouts (View system — no Compose) |
| Architecture | MVVM — ViewModel + LiveData |
| Storage | SharedPreferences (no database needed) |
| Widgets | AppWidgetProvider |
| Background | Foreground Service (keeps flash alive) |
| Permissions | CAMERA (torch), WAKE_LOCK, VIBRATE |

---

## 📦 Project Setup in Android Studio / VS Code

```
1. File → New → New Project → Empty Activity
2. Package name: com.crosssafe.app
3. Language: Kotlin
4. Min SDK: API 26
5. Copy all files from this guide into the project
```

---

## 🚀 Build Order (follow this sequence)

1. `README_FLASH_ENGINE.md` — build the core flash logic first
2. `README_SCREENS.md` — build Home + Flash screens
3. `README_PRESETS.md` — add preset system
4. `README_GESTURES_EXIT.md` — wire up all exit methods
5. `README_ONBOARDING.md` — add first-launch flow
6. `README_SETTINGS.md` — settings screen
7. `README_WIDGETS.md` — home screen widgets (do last)

---

## ✅ Core Rules (never break these)

- App works **100% offline** — no internet, no ads, no tracking
- Flash screen must **always** have a way to exit (6 methods)
- Brightness goes to **100%** automatically on flash start
- **WakeLock** is always held during flash so screen never sleeps
- Epilepsy warning shown on **first launch only**, cannot be skipped
- App must work on **Android 8.0+**
- All features must work on **budget Android phones** (low RAM)
