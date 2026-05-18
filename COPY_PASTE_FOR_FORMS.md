# 📋 COPY-PASTE TEXT FOR PLAY CONSOLE FORMS

Use this document to quickly fill out the Play Console permission forms.

---

## ✅ FORM 1: FOREGROUND SERVICE (Most Important!)

### Question: Does your app use foreground services?
**Select:** YES

### Question: Select the type(s):
**Check ONLY this box:**
- ✅ Special use

**Leave UNCHECKED:**
- ☐ Camera
- ☐ Location  
- ☐ Media playback
- ☐ Microphone
- ☐ Phone call
- ☐ All others

### Question: Explain the special use

**COPY THIS TEXT:**

```
CrossSafe is a pedestrian safety app that keeps the phone screen flashing bright colors while users cross roads at night to increase visibility to drivers.

The foreground service keeps the screen flashing and prevents the system from killing the app during critical safety moments. 

User visibility: A persistent notification displays "CrossSafe is active - Screen is flashing" with a STOP button. The screen actively flashes at 100% brightness.

User control: Service only starts when user taps GO button. It stops when user taps STOP in notification, exits the app, timer expires, or shakes phone.

Why specialUse: This unique safety feature doesn't fit standard categories (not camera, location, media, etc.). We control the flashlight but don't access camera frames. The main purpose is keeping the screen flashing, which doesn't fit any standard foreground service type.

The service must run when app is in background because users may minimize the app while crossing. The notification keeps them informed with immediate STOP control.
```

### Question: Does the service run when the app is in the background?
**Select:** YES

**Click:** SAVE

---

## ✅ FORM 2: NOTIFICATIONS

### Question: Does your app send notifications?
**Select:** YES

### Question: What are the notifications for?

**COPY THIS TEXT:**

```
Displays a persistent notification while the safety flash is active. The notification shows:
- "CrossSafe is active"  
- "Screen is flashing — tap STOP to end"
- STOP button to immediately end the flash
- Tap-to-open to return to the flash screen

This notification is required for the foreground service and provides essential user control.
```

**Click:** SAVE

---

## ✅ FORM 3: EXACT ALARMS

### Question: Does your app use exact alarms?
**Select:** YES

### Question: Why does your app need exact alarms?

**COPY THIS TEXT:**

```
The app provides a timer feature for the safety flash (default 30 seconds). The exact alarm ensures the flash stops precisely when the timer expires, which is critical for the safety feature to work reliably. Users depend on the timer stopping the flash at the expected time while crossing roads.
```

**Click:** SAVE

---

## ✅ DONE!

After saving all 3 forms:
1. Go back to your release page
2. Permission errors should be gone
3. Click "Review release"
4. Click "Start rollout to Production"
5. Submit! 🎉

---

## 🔍 How to Find the Forms

**Method 1 (Easiest):**
- Click the error message link: "Go to Sensitive app permissions"

**Method 2:**
- Left menu → App content
- Scroll down to find:
  - "Foreground service" card → Click "Manage"
  - "Notifications" card → Click "Manage"  
  - "Alarms & reminders" card → Click "Manage"

**Method 3:**
- Use search box at top of Play Console
- Search for: "foreground service" or "notifications" or "alarms"

---

**That's it! Just copy-paste the text above into the 3 forms.** ✅

