package com.example.reminderly.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.footy.database.ReminderDatabase
import com.example.footy.database.ReminderDatabase.Companion.getInstance
import com.example.reminderly.R
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private val REMINDER_CHANNEL_ID = "reminder_notification_channel"

class AlarmService : Service() {



    private lateinit var mNotifyManager: NotificationManager
    private var mStartId = 0 //identifier for this specific service call
    private val disposable = CompositeDisposable()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mStartId = startId
        val reminderId = intent?.getLongExtra("reminderId", -1L)
        Log.d("DebugTag", "onStartCommandAlarmService:$reminderId ")

        if (reminderId != null) {
            setupNotificationChannel(reminderId, applicationContext)
        }


        return super.onStartCommand(intent, flags, startId)
    }


    private fun setupNotificationChannel(
        reminderId: Long,
        context: Context
    ) {
        mNotifyManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {
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

        sendReminderNotification(reminderId, context)

    }


    /**Create notification with unique id so we can cancel it later*/
    private fun sendReminderNotification(
        reminderId: Long,
        context: Context
    ) {

        //postpone reminder pending to pass to notification builder as action
        val postponeReminderIntent = Intent(context, PostponeActivity::class.java)
        postponeReminderIntent.putExtra("reminderId", reminderId)
        postponeReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val postponeReminderPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(), postponeReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        //new reminder pending intent to pass to notification builder as action
        val endReminderIntent = Intent(context, DoneReminderReceiver::class.java)
        endReminderIntent.putExtra("reminderId", reminderId)
        endReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val endReminderPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(), endReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        //get reminder text using reminder id
        val reminderDatabaseDao = getInstance(context).reminderDatabaseDao
        disposable.add(reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
            .observeOn(
                AndroidSchedulers.mainThread()
            )
                //todo play sound+vibration+pop up notification in loop until PostponeActivity or DoneReminderReceiver stop this service
            .subscribe { reminder ->
                //play audio in loop
                if (reminder.reminderType == 1) {
                    val mediaPlayer = MediaPlayer.create(context, R.raw.alarm as Int)
                    mediaPlayer?.start()
                    mediaPlayer.setOnCompletionListener {  mediaPlayer?.start() }
                }


                val notificationBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                notificationBuilder.apply {
                    setContentText(reminder.text)
                    setSmallIcon(R.drawable.ic_notification_white)
                    addAction(
                        R.drawable.ic_done_white,
                        context.getString(R.string.end_reminder),
                        endReminderPendingIntent
                    )
                    addAction(
                        R.drawable.ic_access_time_white
                        , context.getString(R.string.delay_reminder)
                        , postponeReminderPendingIntent
                    )
                    priority = NotificationCompat.PRIORITY_HIGH
                    setDefaults(NotificationCompat.DEFAULT_ALL)
                    setAutoCancel(true)
                }


               // startForeground(reminder.id.toInt(), notificationBuilder.build())
               mNotifyManager.notify(reminder.id.toInt(), notificationBuilder?.build())

            })


    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf(mStartId)
    }

    override fun onCreate() {
        super.onCreate()

    }


}
