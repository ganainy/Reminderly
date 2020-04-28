package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.reminderly.R
import com.example.reminderly.Utils.ALLOW_PERSISTENT_NOTIFICATION
import com.example.reminderly.Utils.MyUtils

class PersistentNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        MyUtils.cancelPersistentNotification(context)
        MyUtils.putInt(context, ALLOW_PERSISTENT_NOTIFICATION, 1)
        MyUtils.showCustomToast(context, R.string.persistent_notif_disabled, Toast.LENGTH_LONG)
    }
}
