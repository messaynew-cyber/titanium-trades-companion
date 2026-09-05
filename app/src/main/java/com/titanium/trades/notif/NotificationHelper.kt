package com.titanium.trades.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.titanium.trades.MainActivity
import com.titanium.trades.R

/**
 * Builds + posts price/trade alerts as Android notifications.
 * Two channels: CRITICAL alerts (high priority, loud) and price updates (default).
 */
object NotificationHelper {

    private const val CHANNEL_CRITICAL = "trades_critical"
    private const val CHANNEL_INFO = "trades_info"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val critical = NotificationChannel(
            CHANNEL_CRITICAL, "Trade Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Stop-loss and take-profit critical alerts"
            enableVibration(true)
        }
        val info = NotificationChannel(
            CHANNEL_INFO, "Price Updates", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General price and position updates"
        }
        nm.createNotificationChannel(critical)
        nm.createNotificationChannel(info)
    }

    fun post(context: Context, title: String, message: String, isCritical: Boolean) {
        val channel = if (isCritical) CHANNEL_CRITICAL else CHANNEL_INFO
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // POST_NOTIFICATIONS runtime permission should have been requested by activity.
        }
        try {
            nm.notify(System.currentTimeMillis().toInt() / 1000 % 100000, builder.build())
        } catch (_: SecurityException) {
            // Notification permission not granted — silently skip.
        }
    }
}
