package dev.ganainy.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.ganainy.reminderly.services.AlarmService

/**force stop service after 130 second if it didn't stop after 120 second by itself, maybe not needed
 * but won't hurt*/
class StopAlarmServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       context.stopService(Intent(context,AlarmService::class.java))
    }
}
