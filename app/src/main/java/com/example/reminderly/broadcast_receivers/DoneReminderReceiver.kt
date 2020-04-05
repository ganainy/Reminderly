package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.footy.database.ReminderDatabase
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.R
import com.example.reminderly.Utils.DONE_ACTION_FOR_REPEATING_REMINDERS
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.Utils.REMINDER_ID
import com.example.reminderly.database.Reminder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**this receiver is called when the user presses finish(mark as done) button on a notification*/
class DoneReminderReceiver : BroadcastReceiver() {

    val disposable=CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {

               val reminderId = intent.extras?.get(REMINDER_ID) as Long

        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao

        //get reminder by id and set it to done or just close this notification and reminder will
        // work normally in next repeat if it is repeating alarm
        disposable.add( reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { reminder ->

            Log.d("DebugTag", "onReceivetarke: ${MyUtils.getInt(context,DONE_ACTION_FOR_REPEATING_REMINDERS) }")

            /**
             *  doneActionForRepeatingReminders:Int
             *  0-> just cancel this notification and it will work normally in next repeat (default)
             *  1-> end the whole reminder
             *  */
            if (reminder.repeat!=0){
                //repeating reminder
                if (MyUtils.getInt(context,DONE_ACTION_FOR_REPEATING_REMINDERS) == 0){
                    //stop the service to close any ongoing alarm/notification for this reminder
                    MyUtils.stopAlarmService(context)
                }else{
                    //stop the service to close any ongoing alarm/notification for this reminder
                    MyUtils.stopAlarmService(context)
                    //make the reminder done (won't fire alarm/notification again)
                    endReminder(reminder, reminderId, context, reminderDatabaseDao)
                }
            }else{
                //one time reminder
                MyUtils.stopAlarmService(context)
            }


        })

    }

    private fun endReminder(
        reminder: Reminder,
        reminderId: Long,
        context: Context,
        reminderDatabaseDao: ReminderDatabaseDao
    ) {
        reminder.isDone = true
        MyUtils.cancelNotification(reminderId, context)
       disposable.add( reminderDatabaseDao.update(reminder).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(
            {//complete
                MyUtils.cancelAlarmManager(reminderId, context)
                disposable.clear()
            },
            { error ->
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                disposable.clear()
            }
        ))
    }

}
