# CrossSafe — Presets System

All 25+ built-in presets, categories, home pinning, custom presets, and the preset data model.

---

## Preset Data Model

```kotlin
// Location: app/src/main/java/com/crosssafe/app/model/Preset.kt

data class Preset(
    val id: String,                    // unique ID e.g. "police", "red_only", "sos"
    val name: String,                  // display name e.g. "Police"
    val emoji: String,                 // emoji shown on chip e.g. "🔵🔴"
    val category: PresetCategory,
    val colors: List<Int>,             // list of Color ints to cycle through
    val intervalMs: Long,              // flash speed in milliseconds
    val patternType: PatternType,      // see FlashEngine README
    val torchEnabled: Boolean = true,
    val isBuiltIn: Boolean = true,     // false = user-created custom preset
    val isPinned: Boolean = false,     // pinned to home screen
    val chipColor: Int = Color.parseColor("#EF4444")  // color of home chip
)

enum class PresetCategory(val label: String) {
    EMERGENCY("Emergency"),
    SINGLE("Single color"),
    MULTI("Multi color"),
    PATTERN("Pattern"),
    NIGHT("Night mode"),
    CUSTOM("My presets")
}
```

---

## All Built-In Presets

### Category: Emergency

| ID | Name | Colors | Speed | Pattern | Torch |
|---|---|---|---|---|---|
| `police` | Police | RED + BLUE | 300ms | ALTERNATING | ON |
| `ambulance` | Ambulance | RED + WHITE | 250ms | ALTERNATING | ON |
| `fire` | Fire truck | RED + WHITE | 150ms | ALTERNATING | ON |
| `sos` | SOS | RED | — | SOS morse | ON |
| `hazard` | Hazard | AMBER | 500ms | DOUBLE_FLASH | ON |
| `emergency_white` | Bright alert | WHITE | 200ms | ALTERNATING | ON |

### Category: Single Color

| ID | Name | Colors | Speed | Pattern | Torch |
|---|---|---|---|---|---|
| `white_only` | Pure white | WHITE | 600ms | ALTERNATING | ON |
| `red_only` | Red only | RED | 400ms | ALTERNATING | ON |
| `blue_only` | Blue only | BLUE | 400ms | ALTERNATING | ON |
| `amber_only` | Amber | AMBER | 500ms | ALTERNATING | ON |
| `green_only` | Green | GREEN | 500ms | ALTERNATING | OFF |
| `yellow_only` | Yellow | YELLOW | 400ms | ALTERNATING | ON |
| `cyan_only` | Cyan | CYAN | 400ms | ALTERNATING | OFF |
| `magenta_only` | Magenta | MAGENTA | 400ms | ALTERNATING | OFF |

### Category: Multi Color

| ID | Name | Colors | Speed | Pattern | Torch |
|---|---|---|---|---|---|
| `red_blue` | Red + Blue | RED, BLUE | 300ms | ALTERNATING | ON |
| `red_white` | Red + White | RED, WHITE | 350ms | ALTERNATING | ON |
| `blue_white` | Blue + White | BLUE, WHITE | 350ms | ALTERNATING | ON |
| `tricolor` | Tricolor | RED, WHITE, BLUE | 300ms | SEQUENTIAL | ON |
| `rainbow` | Rainbow | RED,ORANGE,YELLOW,GREEN,BLUE,MAGENTA | 200ms | SEQUENTIAL | OFF |
| `warm` | Warm flash | RED, ORANGE, YELLOW | 250ms | SEQUENTIAL | ON |
| `cool` | Cool flash | BLUE, CYAN, GREEN | 250ms | SEQUENTIAL | OFF |
| `india` | India (flag) | ORANGE, WHITE, GREEN | 400ms | SEQUENTIAL | ON |

### Category: Pattern

| ID | Name | Colors | Speed | Pattern |
|---|---|---|---|---|
| `slow_pulse` | Slow pulse | WHITE | 1200ms | ALTERNATING |
| `medium_blink` | Medium blink | WHITE | 600ms | ALTERNATING |
| `rapid_strobe` | Rapid strobe | WHITE | 100ms | ALTERNATING |
| `heartbeat` | Heartbeat | RED | — | HEARTBEAT |
| `double_flash` | Double flash | WHITE | — | DOUBLE_FLASH |
| `triple_flash` | Triple flash | WHITE | — | TRIPLE_FLASH |
| `sos_white` | SOS (white) | WHITE | — | SOS |

### Category: Night Mode (lower intensity, easier on eyes)

| ID | Name | Colors | Speed | Notes |
|---|---|---|---|---|
| `night_red` | Dim red | `#CC0000` | 1000ms | Red at 70% brightness |
| `night_blue` | Soft blue | `#004499` | 800ms | Blue at 60% brightness |
| `night_amber` | Low amber | `#CC6600` | 900ms | Amber at 65% brightness |
| `night_white` | Night white | WHITE | 800ms | White but brightness set to 70% |
| `night_green` | Stealth green | `#006600` | 1000ms | Very low intensity |

---

## Home Screen Pinning

### Rules
- Max 4 presets pinned to home at once
- Default pinned presets (on first install): `police`, `red_only`, `white_only`, `amber_only`
- Pinned presets show as chips in the 2×2 grid on MainActivity
- If all 4 slots full and user tries to pin another: show dialog "Replace which preset?"

### Pin/Unpin Actions
- Long-press chip on home → popup menu: "Remove from home" / "Edit"
- In Presets bottom sheet: star icon on each card (filled = pinned)
- In Settings → Presets → Manage pinned presets: drag-to-reorder list

### Storing Pinned Presets

```kotlin
// SharedPreferences key: "pinned_preset_ids"
// Format: comma-separated IDs e.g. "police,red_only,white_only,amber_only"

fun savePinnedPresets(ids: List<String>) {
    prefs.edit().putString("pinned_preset_ids", ids.joinToString(",")).apply()
}

fun loadPinnedPresets(): List<String> {
    val raw = prefs.getString("pinned_preset_ids", "police,red_only,white_only,amber_only")
    return raw?.split(",") ?: listOf("police", "red_only", "white_only", "amber_only")
}
```

---

## Active Preset Selection

- The currently selected preset is highlighted on home screen (colored glow border)
- GO button text changes to "Start [Preset Name]"
- Last-used preset is automatically selected when app opens
- Selecting a preset does NOT start the flash — only GO button starts it

```kotlin
// SharedPreferences key: "last_preset_id"
// Default: "police"

fun saveLastPreset(id: String) {
    prefs.edit().putString("last_preset_id", id).apply()
}

fun loadLastPreset(): String {
    return prefs.getString("last_preset_id", "police") ?: "police"
}
```

---

## Custom Presets

Users can create up to 5 custom presets.

### Create Custom Preset Screen (bottom sheet)

```
┌─────────────────────────────┐
│  Create preset              │
│                             │
│  Name: [____________]       │  ← text input
│                             │
│  Color 1: [████] pick       │  ← color picker (circle, opens color dialog)
│  Color 2: [████] pick       │  ← optional second color
│  + Add color 3              │  ← optional third color
│                             │
│  Speed: ○ Slow ● Medium     │  ← radio group
│         ○ Fast  ○ Rapid     │
│                             │
│  Pattern: [Alternating ▾]   │  ← spinner
│                             │
│  Torch: [──────●] ON        │  ← toggle
│                             │
│  [Preview]  [Save Preset]   │
└─────────────────────────────┘
```

### Storing Custom Presets

```kotlin
// SharedPreferences: store as JSON string
// Key: "custom_presets"

data class CustomPreset(
    val id: String,         // "custom_1", "custom_2", etc.
    val name: String,
    val colorsHex: List<String>,   // e.g. ["#FF0000", "#0000FF"]
    val intervalMs: Long,
    val patternType: String,
    val torchEnabled: Boolean
)

// Serialize to/from JSON using Gson or kotlinx.serialization
```

---

## PresetRepository Class

```kotlin
// Location: app/src/main/java/com/crosssafe/app/data/PresetRepository.kt

class PresetRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("crosssafe_prefs", Context.MODE_PRIVATE)

    fun getAllPresets(): List<Preset>  // returns built-in + custom presets
    fun getPresetById(id: String): Preset?
    fun getPinnedPresets(): List<Preset>  // returns up to 4 pinned ones
    fun pinPreset(id: String)
    fun unpinPreset(id: String)
    fun getActivePreset(): Preset
    fun setActivePreset(id: String)
    fun saveCustomPreset(preset: Preset)
    fun deleteCustomPreset(id: String)
    fun getPresetsByCategory(category: PresetCategory): List<Preset>
}
```
