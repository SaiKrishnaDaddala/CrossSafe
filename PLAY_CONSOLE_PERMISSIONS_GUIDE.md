# 🎯 Play Console Permission Declarations - Complete Guide

## ⚠️ Errors You're Seeing

```
Error: This release includes permissions that haven't been declared in Play Console.
Error: You must let us know whether your app uses any Foreground Service permissions.
```

## ✅ Solution: Complete These Declarations in Play Console

---

## 📍 Step-by-Step Instructions

### 1️⃣ Go to Play Console Permission Declarations

1. Open [Google Play Console](https://play.google.com/console)
2. Select your app (CrossSafe)
3. In the left menu, navigate to:
   - **App content** (or **Policy** section)
   - Click **App permissions**
   - OR look for **Sensitive app permissions** link in the error message

---

## 🔐 FOREGROUND SERVICE PERMISSION - REQUIRED DECLARATION

### Navigate to Foreground Service Declaration

In Play Console:
- **App content** → **Manage** (under Foreground Service)
- OR click the error link that says "Foreground Service permissions"

### ✅ Question 1: Does your app use Foreground Service?

**Answer:** ✅ **YES**

### ✅ Question 2: Select the Foreground Service type(s)

**Select:** ☑️ **Special use**

**Do NOT select:** camera, location, mediaPlayback, microphone, phoneCall, etc.

### ✅ Question 3: Explain the special use

**Paste this exact text:**

```
CrossSafe is a pedestrian safety application that helps users stay visible to drivers while crossing roads at night. 

The foreground service is used to keep the phone screen actively flashing in bright colors (red, white, orange, etc.) at maximum brightness while the user is crossing the road. This prevents the system from killing the app during the critical safety moments when a pedestrian is crossing.

User visibility:
- A persistent notification is displayed stating "CrossSafe is active" and "Screen is flashing — tap STOP to end"
- The notification includes a visible STOP button that immediately ends the service
- The screen actively flashes bright colors at 100% brightness - highly noticeable to the user

User control:
- The service only starts when the user explicitly taps the "GO" button in the app
- The service stops when:
  * User taps the STOP button in the notification
  * User exits the flash screen (back button, home button, etc.)
  * The user-configured timer expires (default 30 seconds)
  * User shakes the phone (if enabled in settings)

Why specialUse:
- This does not fit any standard foreground service category
- Not camera (we don't access camera frames, only control the torch/flashlight)
- Not mediaPlayback (no audio/video playback)
- Not location (no GPS tracking)
- Not dataSync (no network/data syncing)
- It is a unique safety feature that keeps the screen actively flashing to increase pedestrian visibility at night

The service is essential for user safety and is highly noticeable with clear user controls.
```

### ✅ Question 4: Does the service run when the app is in the background?

**Answer:** ✅ **YES**

**Explanation:**
```
The service continues to run when the user minimizes the app or turns off the screen while crossing the road. This is necessary for safety - the user may put their phone in their pocket or hold it facing away while crossing. The persistent notification keeps the user informed and provides immediate control via the STOP button.
```

---

## 📋 OTHER PERMISSION DECLARATIONS

### 2️⃣ POST_NOTIFICATIONS Permission

**Navigate to:** App content → Notifications

**Does your app send notifications?** ✅ **YES**

**Notification purpose:**
```
Displays a persistent notification while the safety flash is active, showing:
- "CrossSafe is active"
- "Screen is flashing — tap STOP to end"
- STOP button to immediately end the flash
- Tap-to-open functionality to return to the flash screen

This notification is required for the foreground service and provides essential user control.
```

---

### 3️⃣ SCHEDULE_EXACT_ALARM Permission

**Navigate to:** App content → Alarms & reminders (or Exact alarms)

**Does your app use exact alarms?** ✅ **YES**

**Why does your app need exact alarms?**
```
The app provides a timer feature that allows users to set a precise duration for the safety flash (default 30 seconds). The exact alarm ensures the flash stops precisely when the timer expires, which is critical for the safety feature to work reliably. Users depend on the timer stopping the flash at the expected time while crossing roads.
```

---

### 4️⃣ REQUEST_INSTALL_PACKAGES Permission (If Asked)

**Does your app install packages?** ⚠️ **Check if you actually need this**

**If YES:**
```
Used for in-app update functionality to prompt users to install app updates downloaded through Google Play.
```

**If NO (and you don't use this feature):**
- Remove this permission from AndroidManifest.xml
- Rebuild and upload new AAB

---

### 5️⃣ Other Permissions That DON'T Need Declaration

These permissions in your manifest are **normal permissions** and don't require Play Console declaration:

- ✅ `WAKE_LOCK` - Normal permission (to keep screen on during flash)
- ✅ `VIBRATE` - Normal permission (for haptic feedback)
- ✅ `RECEIVE_BOOT_COMPLETED` - Normal permission (not used for auto-start)
- ✅ `FOREGROUND_SERVICE` - Base permission (covered by special use declaration)

---

## 🎯 Complete Checklist

Before submitting your release, verify you've completed:

### In Play Console - App Content Section:

- [ ] **Foreground Service permissions**
  - [ ] Declared: YES, app uses foreground service
  - [ ] Selected type: Special use ✅
  - [ ] Provided explanation of special use
  - [ ] Explained background operation

- [ ] **Notifications**
  - [ ] Declared: YES, app sends notifications
  - [ ] Explained notification purpose

- [ ] **Exact alarms**
  - [ ] Declared: YES, app uses exact alarms
  - [ ] Explained why exact timing is needed

- [ ] **Privacy Policy** (if not done already)
  - [ ] Added privacy policy URL (if collecting any data)
  - [ ] OR declared: No user data collected

- [ ] **Data safety**
  - [ ] Completed data safety form
  - [ ] If app is fully offline with no data collection, declare this

---

## 📸 What the Forms Look Like

### Foreground Service Form

You'll see something like:

```
┌─────────────────────────────────────────┐
│ Foreground service                      │
├─────────────────────────────────────────┤
│ Does your app use foreground services?  │
│ ○ No                                    │
│ ● Yes                                   │
│                                         │
│ Select all types used:                  │
│ ☑️ Special use                          │
│ ☐ Camera                                │
│ ☐ Location                              │
│ ☐ Media playback                         │
│ ☐ Microphone                             │
│ ... (other types)                       │
│                                         │
│ Explain special use: [text box]        │
│                                         │
│ Does service run in background?         │
│ ● Yes  ○ No                             │
└─────────────────────────────────────────┘
```

---

## ⚡ Quick Actions

### If You DON'T Need REQUEST_INSTALL_PACKAGES:

Remove it from your manifest to simplify declarations:

1. Open `AndroidManifest.xml`
2. Remove line:
   ```xml
   <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
   ```
3. Rebuild AAB
4. Upload new version

### After Completing All Declarations:

1. Save all forms in Play Console
2. Go back to your release
3. Click **Review release**
4. The permission errors should be gone ✅
5. Submit for review

---

## 🔍 Where to Find These Settings

### Method 1: Direct from Error
- Click the error message link "Go to Sensitive app permissions"
- It will take you directly to the declaration forms

### Method 2: Manual Navigation

**Play Console Left Menu:**
```
Your App
├── Release
│   └── Production / Testing
├── Grow
└── Policy
    ├── App content ← START HERE
    │   ├── App access
    │   ├── Ads
    │   ├── Content ratings
    │   ├── Target audience
    │   ├── News apps
    │   ├── Privacy policy
    │   ├── Data safety ← Complete this
    │   ├── Government apps
    │   ├── Financial features
    │   ├── Health
    │   └── App permissions ← Declare here
    └── Developer account
```

**Look for these specific items:**
- **Foreground service** (may be under "Sensitive permissions")
- **Notifications**
- **Alarms & reminders** (exact alarms)

---

## ⏱️ What Happens Next

After you complete the declarations:

1. **Immediate:** Errors will clear from release page
2. **Within minutes:** You can submit the release
3. **Review time:** Google typically reviews in 1-7 days
4. **Questions:** Google may ask follow-up questions - respond with the same explanations

---

## 💡 Pro Tips

### 1. Be Specific and Factual
- Don't use vague language
- Explain exactly what the permission does in your app
- Mention user control and visibility

### 2. Mention Safety Aspect
- Your app is a safety tool - emphasize this
- Explain why the permission is critical for user safety
- Show how users control the feature

### 3. Screenshots Help
- If Play Console allows, upload screenshots showing:
  - The notification with STOP button
  - The flash screen in action
  - The settings/timer controls

### 4. Keep Consistency
- Use the same explanations I provided
- They match what's in your manifest and code
- They align with your app's actual behavior

---

## 🆘 If You Still Get Errors

### Error: "Foreground service type not declared"

**Solution:** Make sure you selected "Special use" checkbox in the form

### Error: "Insufficient explanation"

**Solution:** Use the full explanation text I provided above - it covers:
- What the service does
- Why it's noticeable
- How users control it
- Why it doesn't fit standard categories

### Error: "Background operation not justified"

**Solution:** Add this to explanation:
```
The service must continue when the app is in background because users may minimize the app or turn off the screen while crossing the road for safety. The persistent notification keeps them informed and provides immediate control via the STOP button.
```

---

## 📝 Summary

**What you need to do RIGHT NOW:**

1. **Go to Play Console** → Your app → App content
2. **Find "Foreground service"** declaration
3. **Fill in:**
   - YES, uses foreground service
   - Type: Special use ✅
   - Copy-paste the explanation I provided
   - YES, runs in background
4. **Find "Notifications"** declaration
   - YES, sends notifications
   - Copy-paste notification explanation
5. **Find "Alarms"** declaration
   - YES, uses exact alarms
   - Copy-paste alarm explanation
6. **Save all forms**
7. **Go back to release** → Review → Submit

**Time needed:** 5-10 minutes  
**Result:** Errors will clear, release can be submitted ✅

---

**Date:** May 18, 2026  
**Status:** ⚠️ Awaiting Play Console declarations  
**Action Required:** Complete permission forms in Play Console  
**Estimated Time:** 5-10 minutes

**Your code is perfect - this is just paperwork!** 📝✅

