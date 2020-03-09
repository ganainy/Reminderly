package com.example.reminderly.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.reminderly.R


private val REMINDER_CHANNEL_ID = "reminder_notification_channel"

class NewReminderReceiver : BroadcastReceiver() {

    private lateinit var mNotifyManager: NotificationManager

    override fun onReceive( context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminderId",-1L)
        val reminderText = intent.getStringExtra("reminderText")
        sendReminderNotification(reminderId,reminderText,context)
    }

    /**show notification to notify user about a reminder*/
    private fun sendReminderNotification(
        reminderId: Long,
        reminderText: String?,
        context: Context
    ) {
        mNotifyManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminder Notification", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notification for certain reminder"
            mNotifyManager.createNotificationChannel(notificationChannel)
        }

        /**Create notification with unique id so we can cancel it later*/
        val notificationId=  SystemClock.currentThreadTimeMillis().toInt()
        val notificationBuilder = getReminderNotificationBuilder(reminderId,reminderText,notificationId,context)
        mNotifyManager.notify(notificationId, notificationBuilder?.build())

    }


    private fun getReminderNotificationBuilder(
        reminderId: Long,
        reminderText: String?,
        notificationId: Int,
        context: Context
    ): NotificationCompat.Builder? {

        /**new reminder pending intent to pass to notification builder action*/
        val endReminderIntent = Intent(context, DoneReminderReceiver::class.java)
        endReminderIntent.putExtra("reminderId",reminderId)
        endReminderIntent.putExtra("notificationId",notificationId)
        endReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val endReminderPendingIntent = PendingIntent.getBroadcast(
            context,
            SystemClock.currentThreadTimeMillis().toInt(), endReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )


        return NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setContentText(reminderText)
            .setSmallIcon(R.drawable.ic_notification_white)
            .addAction(
                R.drawable.ic_done_white,
                context.getString(R.string.end_reminder),
                endReminderPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
    }


}
