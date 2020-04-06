package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.footy.database.ReminderDatabase
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.Utils.DONE_ACTION_FOR_REMINDERS
import com.example.reminderly.Utils.DONE_ACTION_FOR_REPEATING_REMINDERS
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.database.Reminder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**this receiver is called when the user presses finish(mark as done) button on a notification*/
class DoneReminderReceiver : BroadcastReceiver() {

    val disposable = CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {


        //stop any ongoing alarm/notification
        MyUtils.stopAlarmService(context)

        val reminderId = intent.extras?.get(REMINDER_ID) as Long
        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao

        //get reminder by id and set it to done or just close this notification and reminder will
        // work normally in next repeat if it is repeating alarm
        disposable.add(reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
            .observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe { reminder ->

                Log.d(
                    "DebugTag",
                    "DoneReminderReceiver DONE_ACTION_FOR_REPEATING_REMINDERS: ${MyUtils.getInt(
                        context,
                        DONE_ACTION_FOR_REPEATING_REMINDERS
                    )}"
                )
                Log.d("DebugTag", "DoneReminderReceiver onReceive reminderid: ${reminderId}")

                /**
                 *  DONE_ACTION_FOR_REPEATING_REMINDERS:Int
                 *  this value changes based on user settings
                 *  0-> just cancel this notification and it will work normally in next repeat (default)
                 *  1-> end the whole reminder
                 *  */

                /**
                 *  DONE_ACTION_FOR_REMINDERS:Int
                 *  this value changes based on user settings
                 *  0-> done reminder are saved and can be accessed through menu (default)
                 *  1-> done reminders are deleted
                 *  */


                when {reminder.repeat != 0 && MyUtils.getInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS) == 0 -> {
                        //repeating reminder && should just cancel this notification and reminder will work normally in next repeat (default)
                        //do nothing since we already called stopAlarmService()
                    }
                    else -> {
                        if (MyUtils.getInt(context, DONE_ACTION_FOR_REMINDERS) == 0) {
                            //make the reminder done (won't fire alarm/notification again)
                            markReminderAsDone(reminder, context, reminderDatabaseDao)
                        } else {
                            //delete reminder
                            deleteReminder(reminderDatabaseDao, reminder, context)
                        }

                    }
                }


            })

    }

    private fun deleteReminder(
        reminderDatabaseDao: ReminderDatabaseDao,
        reminder: Reminder,
        context: Context
    ) {
        disposable.add(reminderDatabaseDao.delete(reminder).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(
            {//complete
            },
            { error ->
                MyUtils.showErrorToast(context)
                disposable.clear()
            }
        ))
    }

    private fun markReminderAsDone(
        reminder: Reminder,
        context: Context,
        reminderDatabaseDao: ReminderDatabaseDao
    ) {
        reminder.isDone = true
        disposable.add(reminderDatabaseDao.update(reminder).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(
            {//complete
            },
            { error ->
                MyUtils.showErrorToast(context)
                disposable.clear()
            }
        ))
    }

}
