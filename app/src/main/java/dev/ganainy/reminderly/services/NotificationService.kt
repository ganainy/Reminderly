package dev.ganainy.reminderly.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.Utils.REMINDER_ID
import dev.ganainy.reminderly.broadcast_receivers.DoneReminderReceiver
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private const val NOTIFICATION_REMINDER_CHANNEL_ID = "notification_reminder_notification_channel"
private val disposable = CompositeDisposable()

class NotificationService : Service() {

    private  val notificationManager by lazy {  getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager }
    private val reminderDatabaseDao by lazy {  ReminderDatabase.getInstance(this).reminderDatabaseDao}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val reminderId = intent?.getLongExtra(REMINDER_ID, -1L)

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


    /**Create and send notification with unique id so we can cancel it later*/
    private fun sendReminderNotification(
        reminderId: Long,
        context: Context
    ) {

        //get reminder text using reminder id
        disposable.add(reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reminder ->

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
            setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            setVisibility(VISIBILITY_PUBLIC)
            setSound(Uri.parse("android.resource://"+this@NotificationService.packageName+"/"+ R.raw.tone))
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
            notificationChannel.setSound(Uri.parse("android.resource://"+this.packageName+"/"+ R.raw.tone), null)
            notificationManager.createNotificationChannel(notificationChannel)
        }


    }

}
