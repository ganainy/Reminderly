package dev.ganainy.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.ALLOW_PERSISTENT_NOTIFICATION
import dev.ganainy.reminderly.Utils.MyUtils

class PersistentNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        MyUtils.cancelPersistentNotification(context)
        MyUtils.putInt(context, ALLOW_PERSISTENT_NOTIFICATION, 1)
        MyUtils.showCustomToast(context, R.string.persistent_notif_disabled, Toast.LENGTH_LONG)
    }
}
