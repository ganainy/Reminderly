package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.reminderly.services.AlarmService

class StopAlarmServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DebugTag", "StopAlarmServiceReceiver->onReceive: ")
       context.stopService(Intent(context,AlarmService::class.java))
    }
}
