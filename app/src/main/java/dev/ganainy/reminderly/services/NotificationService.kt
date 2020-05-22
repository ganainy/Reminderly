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
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.Utils.MyUtils.Companion.getReminderFromString
import dev.ganainy.reminderly.Utils.MyUtils.Companion.getStringFromReminder
import dev.ganainy.reminderly.Utils.REMINDER
import dev.ganainy.reminderly.broadcast_receivers.DoneReminderReceiver
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.ui.postpone_activity.PostponeActivity
import timber.log.Timber

private const val NOTIFICATION_REMINDER_CHANNEL_ID = "notification_reminder_notification_channel"

class NotificationService : Service() {

    private  val notificationManager by lazy {  getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Timber.d("Timber, service called")
        val count=MyUtils.getInt(this,"service")
        MyUtils.putInt(this,"service",count+1)

        val reminderString = intent?.getStringExtra(REMINDER)
        val reminder=reminderString?.getReminderFromString()?: return super.onStartCommand(intent, flags, startId)

        /**only send reminder if we are not in dnd period*/
        if (!MyUtils.isDndPeriod(this)) {
            sendReminderNotification(reminder, applicationContext)
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
        reminder: Reminder,
        context: Context
    ) {

                val notificationBuilder = getNotificationBuilder(
                    context,
                    reminder
                )
                notificationManager.notify(reminder.id.toInt(), notificationBuilder.build())
            }


    private fun getNotificationBuilder(
        context: Context,
        reminder: Reminder
    ): NotificationCompat.Builder {

        val reminderId = reminder.id

        //postpone reminder pending to pass to notification builder as action
        val postponeReminderIntent = Intent(context, PostponeActivity::class.java)
        postponeReminderIntent.putExtra(REMINDER, reminder.getStringFromReminder())
        postponeReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val postponeReminderPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(), postponeReminderIntent, PendingIntent.FLAG_ONE_SHOT
        )

        //new reminder pending intent to pass to notification builder as action
        val endReminderIntent = Intent(context, DoneReminderReceiver::class.java)
        endReminderIntent.putExtra(REMINDER, reminder.getStringFromReminder())
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
