# Google Play Console - Special Use Foreground Service Declaration

## For Play Console Submission

When uploading the app to Google Play Console, you'll be asked to justify the use of `FOREGROUND_SERVICE_SPECIAL_USE` permission.

### Declaration Text for Play Console Form

**Use this text when submitting your app/update:**

---

**Special Use Foreground Service Justification:**

CrossSafe is a pedestrian safety application that helps users stay visible to drivers while crossing roads at night. The foreground service is used to:

1. **Purpose**: Keep the phone screen flashing in bright colors (red, white, orange, etc.) at maximum brightness while the user is crossing the road
2. **User visibility**: A persistent notification is displayed stating "CrossSafe is active" and "Screen is flashing — tap STOP to end" with a stop action button
3. **User control**: The service only starts when the user explicitly taps the "GO" button to begin flashing, and stops when:
   - User taps the STOP button in the notification
   - User exits the flash screen (back button, home button, etc.)
   - The user-configured timer expires
   - User shakes the phone (if enabled in settings)

4. **Why specialUse**: This doesn't fit any standard foreground service category:
   - Not camera (we don't access camera frames, only control the torch)
   - Not mediaPlayback (not playing audio/video)
   - Not location (no GPS tracking)
   - Not dataSync (no network/sync)
   - It's a unique safety feature that keeps the screen actively flashing

5. **User benefit**: Increases pedestrian visibility at night, reducing accident risk

The service is essential to prevent the screen from turning off during the critical moments when a pedestrian is crossing a road.

---

### Manifest Property Already Included

Your `AndroidManifest.xml` already includes the required property:

```xml
<property
    android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
    android:value="Safety alert - flashing screen to increase pedestrian visibility" />
```

✅ This is correct and should not be changed.

### Screenshots to Include in Play Console

When submitting, include these screenshots to demonstrate the special use:

1. **Main screen** showing the GO button (explains how user initiates the service)
2. **Flash screen** in action (shows the highly visible flashing)
3. **Notification** showing "CrossSafe is active" with the STOP button
4. **Settings screen** showing timer and control options (proves user control)

### Compliance Checklist

- ✅ Service only runs when user explicitly starts flashing
- ✅ Persistent notification with service status and stop button
- ✅ Service stops when user exits or timer expires
- ✅ Manifest property explains the special use
- ✅ Service behavior is clearly noticeable (screen flashing)
- ✅ Unique use case that doesn't fit standard categories

### Additional Notes

- The service does NOT run in background without user awareness
- The service does NOT start automatically on device boot
- The service does NOT collect or transmit any data
- The app works 100% offline with no network permissions
- The foreground service is the ONLY way to keep the screen on and flashing while the user's phone is in their pocket or pointing at traffic

---

**Last Updated:** May 18, 2026  
**App Version:** Check version.properties for current version  
**Target SDK:** 35 (Android 15)

