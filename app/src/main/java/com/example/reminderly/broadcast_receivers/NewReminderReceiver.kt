package com.example.reminderly.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startForegroundService
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.ui.postpone_activity.PostponeActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


/**triggered by alarm manager to show notification when reminder time comes
 * NOTE:we don't call the service directly because if app is asleep it won't trigger so we call
 * receiver and acquire wakelock then call the service and release the wakelock there*/
class NewReminderReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.getLongExtra(REMINDER_ID, -1L)
        Log.d("DebugTag", "onReceiveNewReminderReceiver: $reminderId")

        if (reminderId!=-1L){

        //acquire wakelock
        val wakeLock: PowerManager.WakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire(10 * 1000L /*10 seconds*/)
                }
            }

        //start the service the will show the notification
        val notifyIntent = Intent(context, AlarmService::class.java)
        notifyIntent.putExtra(REMINDER_ID, reminderId)

        startForegroundService(context, notifyIntent)

    }
    }
}
