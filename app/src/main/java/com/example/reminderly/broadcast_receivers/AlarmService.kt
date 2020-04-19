package com.example.reminderly.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.*
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


private val NOTIFICATION_REMINDER_CHANNEL_ID = "notification_reminder_notification_channel"
private val ALARM_REMINDER_CHANNEL_ID = "alarm_reminder_notification_channel"

class AlarmService : Service() {


    private lateinit var mNotifyManager: NotificationManager
    private val disposable = CompositeDisposable()
    private lateinit var mCountDownTimer: CountDownTimer
    private val mediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.tone as Int).apply {
            setVolume(1F, 1F)
            isLooping = true
        }
    }

    val reminderDatabaseDao = ReminderDatabase.getInstance(this).reminderDatabaseDao


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //setup notification channels
        setupAlarmReminderNotificationChannel(applicationContext)
        setupNotificationReminderNotificationChannel(applicationContext)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//first check if any alarm is already active
        if (mediaPlayer.isPlaying) {
            //this means second reminder came while a alert reminder is active so we delay the
            // second reminder for 5 minutes till the first one finishes
            MyUtils.showCustomToast(this, R.string.another_reminder_is_showing, Toast.LENGTH_LONG)
            val secondReminderId = intent?.getLongExtra(REMINDER_ID, -1L)
            if (secondReminderId != null) {
                postponeSecondReminder(secondReminderId)
            }
            return super.onStartCommand(intent, flags, startId)
        }


        val reminderId = intent?.getLongExtra(REMINDER_ID, -1L)

        /**only send reminder if we are not in dnd period*/
        if (reminderId != null && reminderId != -1L && !inDndPeriod()) {
            sendReminderNotification(reminderId, applicationContext)
        }


        return super.onStartCommand(intent, flags, startId)
    }

    /** if an alarm fires up when other alarm is ongoing it will be automatically delayed for 5
     * minutes until the first ends*/
    private fun postponeSecondReminder(secondReminderId: Long) {
        Log.d("DebugTag", "postponeSecondReminder: called")
        val disposable =
            reminderDatabaseDao.getReminderById(secondReminderId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val postponedReminder = MyUtils.forcePostponeReminder(it, 0, 0, 5)
                    reminderDatabaseDao.update(postponedReminder).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({
                            //done
                        },{
                            MyUtils.showCustomToast(this,R.string.something_went_wrong)
                        })
                    disposable.clear()
                }

    }

    /**this method checks if a date is in range between two dates*/
    private fun inDndPeriod(): Boolean {

        //first check if dnd option is enabled
        if (MyUtils.getInt(applicationContext, DND_OPTION_ENABLED) == 0) {
            //dnd option is disabled
            return false
        }

        val startMinute = MyUtils.getInt(applicationContext, DONT_DISTURB_START_MINUTES)
        val startHour = MyUtils.getInt(applicationContext, DONT_DISTURB_START_HOURS)
        val endHour = MyUtils.getInt(applicationContext, DONT_DISTURB_END_HOURS)
        val endMinute = MyUtils.getInt(applicationContext, DONT_DISTURB_END_MINUTES)

        //get current minute and hour and compare them with dnd period
        val currentTime = Calendar.getInstance()

        val dndStart = Calendar.getInstance()
        dndStart.set(Calendar.HOUR_OF_DAY, startHour)
        dndStart.set(Calendar.MINUTE, startMinute)

        val dndEnd = Calendar.getInstance()
        dndEnd.set(Calendar.HOUR_OF_DAY, endHour)
        dndEnd.set(Calendar.MINUTE, endMinute)

        return currentTime.after(dndStart) && currentTime.before(dndEnd)

    }


    private fun setupNotificationReminderNotificationChannel(
        context: Context
    ) {
        mNotifyManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
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
            notificationChannel.description = "Notification for certain notification reminder"
            mNotifyManager.createNotificationChannel(notificationChannel)
        }


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
                if (reminder.reminderType == 1) {
                    //alarm notification (always on screen notification+play sound)
                    startAlarmNotification(context, reminder, reminderId)
                } else {
                    //normal notification
                    val notificationBuilder = getNotificationBuilder(
                        context,
                        reminder,
                        reminderId,
                        NOTIFICATION_REMINDER_CHANNEL_ID
                    )

                    mNotifyManager.notify(reminder.id.toInt(), notificationBuilder.build())

                }


            })


    }

    private fun setupAlarmReminderNotificationChannel(context: Context) {
        mNotifyManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                ALARM_REMINDER_CHANNEL_ID,
                "Reminder Notification", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableVibration(false)
            notificationChannel.description = "Notification for certain alarm reminder"
            mNotifyManager.createNotificationChannel(notificationChannel)
        }

    }


    private fun getNotificationBuilder(
        context: Context,
        reminder: Reminder,
        reminderId: Long,
        notificationChannelId: String
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

        val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
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
            setAutoCancel(true)
        }

        return notificationBuilder


    }

    private fun startAlarmNotification(
        context: Context,
        reminder: Reminder,
        reminderId: Long
    ) {

        // show notification every second so its always on screen && play audio in loop
        mediaPlayer.start()

        val notificationBuilder = getNotificationBuilder(context, reminder, reminderId, ALARM_REMINDER_CHANNEL_ID)

        mCountDownTimer = object : CountDownTimer(3 * 60 * 1000L, 1500) {
            override fun onTick(millisUntilFinished: Long) {
                startForeground(reminder.id.toInt(), notificationBuilder.build())
            }

            override fun onFinish() {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            }
        }.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        if (::mCountDownTimer.isInitialized) {
            mCountDownTimer.cancel()
        }
    }

}
