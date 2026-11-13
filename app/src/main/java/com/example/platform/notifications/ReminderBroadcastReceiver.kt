package com.example.platform.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val festivalId = com.example.platform.security.SecurityIntegrityManager.sanitizeFestivalId(
            intent.getStringExtra("festival_id")
        ) ?: "general"
        val title = com.example.platform.security.SecurityIntegrityManager.sanitizeDisplayText(
            intent.getStringExtra("title"),
            maxLength = 100,
            fallback = "Sacred Festival Reminder"
        )
        val message = com.example.platform.security.SecurityIntegrityManager.sanitizeDisplayText(
            intent.getStringExtra("message"),
            maxLength = 240,
            fallback = "Open UtsavSetu for puja vidhi and muhurat."
        )

        val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("utsavsetu://festival?id=$festivalId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            festivalId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(festivalId.hashCode(), notification)
    }
}
