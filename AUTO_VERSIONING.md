# 🚀 Auto-Versioning System

## Overview
CrossSafe now has **automatic versioning** that increments with every build. No manual version updates needed!

## How It Works

### Version Code (Auto-Incremented)
```kotlin
versionCode = (days_since_epoch * 1000) + (build_number_of_day)
```

**Example:** `19854432`
- Unique integer that always increases
- Based on timestamp - guarantees increment with each build
- Supports ~1000 builds per day
- Never decreases, never conflicts

### Version Name (Auto-Generated)

**Debug Builds:**
```
1.0.0-build.20260514.1430-DEBUG
 │ │ │        │         │
 │ │ │        │         └─ Build time (14:30)
 │ │ │        └─────────── Build date (May 14, 2026)
 │ │ └──────────────────── Patch version
 │ └────────────────────── Minor version
 └──────────────────────── Major version
```

**Release Builds:**
```
1.0.0
 │ │ │
 │ │ └── Patch version (bug fixes)
 │ └──── Minor version (new features)
 └────── Major version (breaking changes)
```

## Configuration

### version.properties
Edit this file to update semantic version:

```properties
MAJOR_VERSION=1  # Breaking changes, major redesign
MINOR_VERSION=0  # New features
PATCH_VERSION=0  # Bug fixes, improvements
```

### When to Increment

| Version | When | Example |
|---------|------|---------|
| **MAJOR** | Breaking changes, complete redesign | 1.0.0 → 2.0.0 |
| **MINOR** | New features, enhancements | 1.0.0 → 1.1.0 |
| **PATCH** | Bug fixes, small improvements | 1.0.0 → 1.0.1 |

## Build Variants

### Debug Build
- **Package:** `com.crosssafe.app.debug`
- **Version:** `1.0.0-build.20260514.1430-DEBUG`
- **Usage:** Testing, development
- **Shows:** Full build info with timestamp

### Release Build  
- **Package:** `com.crosssafe.app`
- **Version:** `1.0.0`
- **Usage:** Play Store, production
- **Shows:** Clean semantic version only

## BuildConfig Fields

Access version info in your code:

```kotlin
// In MainActivity or anywhere
BuildConfig.VERSION_NAME      // "1.0.0-build.20260514.1430"
BuildConfig.VERSION_CODE      // 19854432
BuildConfig.VERSION_INFO      // "v1.0.0-build.20260514.1430 (19854432)"
BuildConfig.BUILD_DATE        // "2026-05-14 14:30:00"
BuildConfig.SIMPLE_VERSION    // "1.0.0"
```

## Usage Examples

### Show Version in About Dialog
```kotlin
private fun showInfoDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("CrossSafe")
        .setMessage("""
            A free, offline app for road safety.
            
            Version: ${BuildConfig.SIMPLE_VERSION}
            Build: ${BuildConfig.VERSION_CODE}
            Date: ${BuildConfig.BUILD_DATE}
        """.trimIndent())
        .setPositiveButton("OK", null)
        .show()
}
```

### Log Version on App Start
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("CrossSafe", "App started: ${BuildConfig.VERSION_INFO}")
}
```

## Benefits

✅ **No Manual Updates** - Versions auto-increment with every build  
✅ **No Duplicates** - Tracks last used code, prevents Play Store conflicts  
✅ **Unique Builds** - Every build has a unique version code  
✅ **Timestamp Tracking** - Know exactly when each build was created  
✅ **Play Store Ready** - Release builds have clean version names  
✅ **Debug Friendly** - Debug builds show full build info  
✅ **Git Independent** - Works without Git (pure Gradle solution)  
✅ **Fast Builds** - No external dependencies or plugins needed  
✅ **Auto-Updating** - version.properties updates itself  

## Version History Tracking

Each build automatically logs its version. You can track versions by:

1. **APK Filename** - Contains version code and variant
   - `app-debug-19854432.apk`
   - `app-release-19854432.apk`

2. **BuildConfig** - Access in code anytime
   ```kotlin
   Log.d("Version", BuildConfig.VERSION_INFO)
   ```

3. **About Screen** - Show to users
   ```kotlin
   "Version ${BuildConfig.VERSION_NAME}"
   ```

## Manual Version Bump

To release a new major/minor/patch version:

1. Open `version.properties`
2. Update the appropriate version:
   ```properties
   MAJOR_VERSION=1
   MINOR_VERSION=1  # New features → 1.0.0 becomes 1.1.0
   PATCH_VERSION=0
   ```
3. Build → Clean Project
4. Build → Rebuild Project
5. Done! Version is now 1.1.0

## APK Distribution

When sharing APKs, the filename includes the version code:

```
app/build/outputs/apk/debug/
├── app-debug.apk              (always latest)
└── app-debug-19854432.apk     (versioned copy)

app/build/outputs/apk/release/
├── app-release.apk            (always latest)
└── app-release-19854432.apk   (versioned copy)
```

## Play Store Deployment

For Play Store releases:

1. Build release APK/AAB
2. Version code auto-increments (never conflicts)
3. Version name shows clean: `1.0.0`
4. Upload to Play Console
5. Play Store accepts it (version code > previous)

## Troubleshooting

### Version not incrementing?
- **Clean Project** → Build → Clean Project
- **Invalidate Caches** → File → Invalidate Caches / Restart

### Want to reset version code?
- Version code resets naturally when time-based formula produces a new value
- Each new day starts with a higher base number

### Need manual control?
Replace auto version code with:
```kotlin
versionCode = 1
versionName = "1.0.0"
```

## Advanced Customization

### Git Commit Count (Alternative)
If you want version based on Git commits instead:

```kotlin
fun getGitCommitCount(): Int {
    return try {
        val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
        process.inputStream.bufferedReader().readText().trim().toInt()
    } catch (e: Exception) {
        1
    }
}
```

### Timestamp Format Options
Change timestamp format in `build.gradle.kts`:

```kotlin
// Current: 20260514.1430
SimpleDateFormat("yyyyMMdd.HHmm", Locale.US)

// Alternative: 2026-05-14_14-30
SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)

// Alternative: 20260514
SimpleDateFormat("yyyyMMdd", Locale.US)
```

## Migration from Old Versioning

Old (Manual):
```kotlin
versionCode = 1
versionName = "1.0.0"
```

New (Automatic):
```kotlin
versionCode = getAutoVersionCode()     // Auto: 19854432
versionName = getAutoVersionName()     // Auto: 1.0.0-build.20260514.1430
```

**No breaking changes!** Existing version references still work:
- `BuildConfig.VERSION_NAME` - Still exists
- `BuildConfig.VERSION_CODE` - Still exists
- App displays correct version everywhere

---

## 🎯 Quick Reference

| What | Value | Where |
|------|-------|-------|
| Major.Minor.Patch | `version.properties` | Edit manually |
| Version Code | Auto-generated | Never edit |
| Version Name | Auto-generated | Shows semantic version + build info |
| Release Version | Clean semantic | `1.0.0` for Play Store |
| Debug Version | With timestamp | `1.0.0-build.20260514.1430-DEBUG` |

**Just build and go! 🚀 Versions handle themselves.**

