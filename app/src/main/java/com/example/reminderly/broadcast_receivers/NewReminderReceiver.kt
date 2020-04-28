package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.services.AlarmService
import com.example.reminderly.services.NotificationService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


/**triggered by alarm manager to show notification when reminder time comes
 * NOTE:we don't call the service directly because if app is asleep it won't trigger so we call
 * receiver and acquire wakelock then call the service and release the wakelock there*/
val disposable = CompositeDisposable()

class NewReminderReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.getLongExtra(REMINDER_ID, -1L)

        print("timeonReceive${System.currentTimeMillis()}")

        if (reminderId != -1L) {

          /*  //acquire wakelock
            val wakeLock: PowerManager.WakeLock =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                        acquire(10 * 1000L *//*10 seconds*//*)
                    }
                }*/

            //start alarm/notification service based on reminder type
            val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
            disposable.add(
                reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
                        //start background service or foreground service based on reminderType

                        print("timeonReceive${System.currentTimeMillis()}")
                        if (it.reminderType == 0) {
                            val notificationServiceIntent =
                                Intent(context, NotificationService::class.java)
                            notificationServiceIntent.putExtra(REMINDER_ID, reminderId)
                            context.startService(notificationServiceIntent)
                        } else {
                            val alarmServiceIntent = Intent(context, AlarmService::class.java)
                            alarmServiceIntent.putExtra(REMINDER_ID, reminderId)
                            context.startService(alarmServiceIntent)
                        }
                        disposable.clear()
                    })


        }
    }
}
