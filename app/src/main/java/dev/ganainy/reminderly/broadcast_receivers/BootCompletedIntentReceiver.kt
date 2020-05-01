package dev.ganainy.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.Utils.MyUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**reschedule alarms after device is rebooted*/
class BootCompletedIntentReceiver : BroadcastReceiver() {

    var compositeDisposable=CompositeDisposable()
    override fun onReceive(context: Context, intent: Intent) {


        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
          restartUpcomingAlarms(context)
        }
    }

    private fun restartUpcomingAlarms(context: Context) {
        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
        compositeDisposable.add (reminderDatabaseDao.getUpcomingReminders(Calendar.getInstance().timeInMillis).subscribeOn(
            Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                for (reminder in it){
                    MyUtils.addAlarmManager(reminder.id,context,reminder.createdAt.timeInMillis,reminder.repeat)
                }
                compositeDisposable.clear()
            })
    }
}
