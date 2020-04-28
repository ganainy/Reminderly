package com.example.reminderly.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.broadcast_receivers.DoneReminderReceiver
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private const val NOTIFICATION_REMINDER_CHANNEL_ID = "notification_reminder_notification_channel"
private val disposable = CompositeDisposable()

class NotificationService : Service() {

    private  val notificationManager by lazy {  getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager }
    private val reminderDatabaseDao by lazy {  ReminderDatabase.getInstance(this).reminderDatabaseDao}
    private val mediaPlayer by lazy { MediaPlayer.create(this, R.raw.tone)}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val reminderId = intent?.getLongExtra(REMINDER_ID, -1L)

        //first check if any alarm is already active
  /*      if (MyUtils.getInt(this, ONGOING_ALARM_FLAG) == 1) {
            //this means second reminder came while a alert reminder is active so we delay the
            // second reminder for 5 minutes till the first one finishes
            MyUtils.showCustomToast(this, R.string.another_reminder_is_showing, Toast.LENGTH_LONG)
            val secondReminderId = intent?.getLongExtra(REMINDER_ID, -1L)
            if (secondReminderId != null) {
                postponeSecondReminder(secondReminderId)
            }
            return super.onStartCommand(intent, flags, startId)
        }*/



        /**only send reminder if we are not in dnd period*/
        if (reminderId != null && reminderId != -1L && !MyUtils.isDndPeriod(this)) {
            sendReminderNotification(reminderId, applicationContext)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationReminderNotificationChannel()

    }


    /**Create notification with unique id so we can cancel it later*/
    private fun sendReminderNotification(
        reminderId: Long,
        context: Context
    ) {

        //get reminder text using reminder id
        disposable.add(reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reminder ->

                //normal notification
                mediaPlayer.start()
                val notificationBuilder = getNotificationBuilder(
                    context,
                    reminder,
                    reminderId
                )
                notificationManager.notify(reminder.id.toInt(), notificationBuilder.build())
            })
    }




    private fun getNotificationBuilder(
        context: Context,
        reminder: Reminder,
        reminderId: Long
    ): NotificationCompat.Builder {

        //postpone reminder pending to pass to notification builder as action
        val postponeReminderIntent = Intent(context, PostponeActivity::class.java)
        postponeReminderIntent.putExtra(REMINDER_ID, reminderId)
        postponeReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val postponeReminderPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(), postponeReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        //new reminder pending intent to pass to notification builder as action
        val endReminderIntent = Intent(context, DoneReminderReceiver::class.java)
        endReminderIntent.putExtra(REMINDER_ID, reminderId)
        endReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val endReminderPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(), endReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_REMINDER_CHANNEL_ID)
        notificationBuilder.apply {
            setContentText(reminder.text)
            setSmallIcon(R.drawable.ic_bell_white)
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
            setVisibility(VISIBILITY_PUBLIC)

            setAutoCancel(true)
        }

        return notificationBuilder


    }





    private fun setupNotificationReminderNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                NOTIFICATION_REMINDER_CHANNEL_ID,
                "Reminder Notification", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility= VISIBILITY_PUBLIC
            notificationChannel.description = "Notification for certain notification reminder"
            notificationManager.createNotificationChannel(notificationChannel)
        }


    }

}
