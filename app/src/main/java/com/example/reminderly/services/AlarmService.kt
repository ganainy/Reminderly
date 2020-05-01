package com.example.reminderly.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.ALLOW_PERSISTENT_NOTIFICATION
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.broadcast_receivers.DoneReminderReceiver
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


private const val ALARM_REMINDER_CHANNEL_ID = "alarm_reminder_notification_channel"
private const val FOREGROUND_SERVICE_CHANNEL_ID = "foregroundServiceChannelId"
private const val ONGOING_SERVICE_ID = 101

class AlarmService : Service() {


    /**millis of the end of today so we can get any reminders after that (upcoming reminders)*/
    private val nextDayMillis: Long
        get() {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)

            }.timeInMillis
        }


    /**millis of the begging of today so we can get any reminders before that (overdue reminders)*/
    private val todayMillis: Long
        get() {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

            }.timeInMillis
        }

    private val disposable = CompositeDisposable()
    private lateinit var mCountDownTimer: CountDownTimer
    private val reminderDatabaseDao by lazy {  ReminderDatabase.getInstance(this).reminderDatabaseDao}
    private  val notificationManager by lazy {getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val reminderId = intent?.getLongExtra(REMINDER_ID, -1L)

        /**only send reminder if we are not in dnd period*/
        if (reminderId != null && reminderId != -1L && !MyUtils.isDndPeriod(this)) {
            disposable.add(reminderDatabaseDao.getReminderById(reminderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { reminder ->
                    startAlarmNotification(this, reminder)
                })


            sendForegroundServiceNotification()

        }

        /**to update passed/upcoming reminders of today (there is similar method in main activity
         *  but this is in case the service fired when application was closed)*/
        observeTodayReminders()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun observeTodayReminders() {
        disposable.add(
            reminderDatabaseDao.getInTimeRangeReminders(todayMillis, nextDayMillis)
                .subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ todayReminders ->


                //show persistent notification to help user add reminder from outside of app
                /**check if user allowed showing persistent notification
                 * 0-> allowed (default)
                 * 1-> not allowed
                 */
                if (MyUtils.getInt(this, ALLOW_PERSISTENT_NOTIFICATION) == 0) {
                    MyUtils.sendPersistentNotification(applicationContext, todayReminders)
                }

            }, { error ->
                MyUtils.showCustomToast(this, R.string.error_retreiving_reminder)

            })
        )
    }


    override fun onCreate() {
        super.onCreate()
        //setup notification channels
        setupAlarmReminderNotificationChannel(applicationContext)
        //setup ongoing service channel & notification to make this service foreground service
        createForegroundServiceNotificationChannel()
    }

    private fun setupAlarmReminderNotificationChannel(context: Context) {
       val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                ALARM_REMINDER_CHANNEL_ID,
                "Reminder Notification", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableVibration(false)
            notificationChannel.description = "Notification for certain alarm reminder"
            notificationChannel.setSound(Uri.parse("android.resource://"+this.packageName+"/"+ R.raw.tone), null)
            notificationManager.createNotificationChannel(notificationChannel)
        }

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

        val notificationBuilder = NotificationCompat.Builder(context, ALARM_REMINDER_CHANNEL_ID)
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
            setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            setSound(Uri.parse("android.resource://"+this@AlarmService.packageName+"/"+ R.raw.tone))
            setAutoCancel(true)
        }

        return notificationBuilder


    }

    private fun startAlarmNotification(
        context: Context,
        reminder: Reminder
    ) {
        val notificationBuilder = getNotificationBuilder(context, reminder, reminder.id)

        mCountDownTimer = object : CountDownTimer(2 * 60 * 1000L, 2500) {
            override fun onTick(millisUntilFinished: Long) {
                notificationManager.notify(reminder.id.toInt(), notificationBuilder.build())
            }

            override fun onFinish() {
               stopSelf()
            }
        }.start()
    }


    /**channel that will be used to create notification so that service is treated as foreground service*/
    private fun createForegroundServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundServiceChannel = NotificationChannel(
                FOREGROUND_SERVICE_CHANNEL_ID,
                "Foreground service channel",
                NotificationManager.IMPORTANCE_LOW
            )
            foregroundServiceChannel.description = "This is foreground service channel"

            notificationManager.createNotificationChannel(foregroundServiceChannel)
        }
    }

    /**notification so that service is treated as foreground service*/
    private fun sendForegroundServiceNotification() {

        val notification: Notification = NotificationCompat.Builder(this, FOREGROUND_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_settings_grey)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.foreground_service))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        startForeground(ONGOING_SERVICE_ID, notification)

    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()

        if (::mCountDownTimer.isInitialized) {
            mCountDownTimer.cancel()
        }
        Log.d("DebugTag", "AlarmService->onDestroy: ")
    }

}
