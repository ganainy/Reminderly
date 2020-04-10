package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.PowerManager.*
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.core.content.ContextCompat.startForegroundService
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.Utils.REMINDER_ID
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**triggered by alarm manager to show notification when reminder time comes
 * NOTE:we don't call the service directly because if app is asleep it won't trigger so we call
 * receiver and acquire wakelock then call the service and release the wakelock there*/
val disposable=CompositeDisposable()
class NewReminderReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.getLongExtra(REMINDER_ID, -1L)
        Log.d("DebugTag", "NewReminderReceiver onReceive : $reminderId")

        if (reminderId!=-1L){

        //acquire wakelock
        val wakeLock: PowerManager.WakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(FULL_WAKE_LOCK , "MyApp::MyWakelockTag").apply {
                    acquire(10 * 1000L /*10 seconds*/)
                }
            }

        //start the service the will show the notification
        val notifyIntent = Intent(context, AlarmService::class.java)
        notifyIntent.putExtra(REMINDER_ID, reminderId)


            val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
            disposable.add(
                reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
                        //start background service or foreground service based on reminderType
                        if (it.reminderType==0){
                            context.startService(notifyIntent)
                        }else{
                            startForegroundService(context, notifyIntent)
                        }
                        disposable.clear()
                })


    }
    }
}
