package com.crosssafe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.crosssafe.app.FlashActivity
import com.crosssafe.app.R

/**
 * Foreground service that keeps the app alive while the screen is flashing for pedestrian safety.
 *
 * COMPLIANCE WITH FOREGROUND_SERVICE_SPECIAL_USE:
 * - Service is highly noticeable: screen actively flashes bright colors at 100% brightness
 * - User-initiated: only starts when user taps GO button in FlashActivity
 * - Clear notification: shows "CrossSafe is active" with visible STOP action
 * - Unique use case: pedestrian safety feature that doesn't fit standard service types
 * - Manifest declares: android:foregroundServiceType="specialUse" with proper justification
 *
 * This service prevents the system from killing the flash functionality while the user
 * is actively crossing a road with their phone flashing for visibility.
 */
class FlashService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForeground(NOTIFICATION_ID, buildNotification())
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val channelId = "crosssafe_flash_channel"
        // Create notification channel (required on Android 8.0+)
        val channel = NotificationChannel(
            channelId, "Safety Flash Active", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Alerts you while CrossSafe is flashing for pedestrian safety"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val stopIntent = Intent(this, FlashService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, FlashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 1, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("CrossSafe is active")
            .setContentText("Screen is flashing — tap STOP to end")
            .setContentIntent(openPending)
            .addAction(R.drawable.ic_notification, "STOP", stopPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val ACTION_START = "com.crosssafe.ACTION_START"
        const val ACTION_STOP = "com.crosssafe.ACTION_STOP"
        const val NOTIFICATION_ID = 1001
    }
}
