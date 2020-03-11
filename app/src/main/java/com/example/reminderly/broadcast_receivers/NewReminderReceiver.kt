package com.example.reminderly.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


private val REMINDER_CHANNEL_ID = "reminder_notification_channel"

class NewReminderReceiver : BroadcastReceiver() {

    private lateinit var mNotifyManager: NotificationManager

    override fun onReceive( context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminderId",-1L)
        setupNotificationChannel(reminderId,context)
    }

    /**show notification to notify user about a reminder*/
    private fun setupNotificationChannel(
        reminderId: Long,
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

        sendReminderNotification(reminderId,context)

    }


    /**Create notification with unique id so we can cancel it later*/
    private fun sendReminderNotification(
        reminderId: Long,
        context: Context
    ) {
        
        //postpone reminder pending to pass to notification builder as action
        val postponeReminderIntent = Intent(context, PostponeActivity::class.java)
        postponeReminderIntent.putExtra("reminderId",reminderId)
        postponeReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val postponeReminderPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(), postponeReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )
        
        //new reminder pending intent to pass to notification builder as action
        val endReminderIntent = Intent(context, DoneReminderReceiver::class.java)
        endReminderIntent.putExtra("reminderId",reminderId)
        endReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val endReminderPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(), endReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        //get reminder text using reminder id
        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
        reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { reminder ->
                val notificationBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                    .setContentText(reminder.text)
                    .setSmallIcon(R.drawable.ic_notification_white)
                    .addAction(
                        R.drawable.ic_done_white,
                        context.getString(R.string.end_reminder),
                        endReminderPendingIntent
                    )
                    .addAction(
                        R.drawable.ic_access_time_white
                    ,context.getString(R.string.delay_reminder)
                    ,postponeReminderPendingIntent
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                mNotifyManager.notify(reminder.id.toInt(), notificationBuilder?.build())

            }





    }


}
