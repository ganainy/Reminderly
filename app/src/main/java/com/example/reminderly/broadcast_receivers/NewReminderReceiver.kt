package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.PowerManager.*
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.core.content.ContextCompat.startForegroundService
import com.example.reminderly.Utils.REMINDER_ID


/**triggered by alarm manager to show notification when reminder time comes
 * NOTE:we don't call the service directly because if app is asleep it won't trigger so we call
 * receiver and acquire wakelock then call the service and release the wakelock there*/
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

        startForegroundService(context, notifyIntent)

    }
    }
}
