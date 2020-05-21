package dev.ganainy.reminderly.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.Utils.REMINDER_ID
import dev.ganainy.reminderly.services.AlarmService
import dev.ganainy.reminderly.services.NotificationService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


/**triggered by alarm manager to show notification when reminder time comes
 * NOTE:we don't call the service directly because if app is asleep it won't trigger so we call
 * receiver and acquire wakelock then call the service and release the wakelock there*/
val disposable = CompositeDisposable()

class NewReminderReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.getLongExtra(REMINDER_ID, -1L)

        if (reminderId != -1L) {
            Timber.d("Timber, receiver called")
            val count= MyUtils.getInt(context,"rec")
            MyUtils.putInt(context,"rec",count+1)

            //start alarm/notification service based on reminder type
            val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
            disposable.add(
                reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
                        //start background service or foreground service based on reminderType

                        if (it.reminderType == 0) {
                            //open notification service and pass reminder id
                            val notificationServiceIntent =
                                Intent(context, NotificationService::class.java)
                            notificationServiceIntent.putExtra(REMINDER_ID, reminderId)
                            context.startService(notificationServiceIntent)
                        } else {
                            //acquire wakelock
                            val wakeLock: PowerManager.WakeLock =
                                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                                    newWakeLock(PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                                        acquire(3*60*1000L /*3 minutes*/)
                                    }
                                }

                            //open alarm service and pass reminder id
                            val alarmServiceIntent = Intent(context, AlarmService::class.java)
                            alarmServiceIntent.putExtra(REMINDER_ID, reminderId)
                            context.startService(alarmServiceIntent)

                            //add safety receiver after 3 minutes that will terminate alarm service
                            //since sometimes service onDestroy is not called
                            scheduleStopServiceReceiver(context)
                        }
                        disposable.clear()
                    })


        }
    }

    /**force stop service after 130 second if it didn't stop after 120 second by itself*/
    private fun scheduleStopServiceReceiver(context: Context) {
        val stopServiceIntent = Intent(context, StopAlarmServiceReceiver::class.java)
        val stopServicePendingIntent = PendingIntent.getBroadcast(
            context, 111, stopServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 130)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                stopServicePendingIntent
            )
        } else {
            alarmManager?.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                stopServicePendingIntent
            )
        }
    }
}
