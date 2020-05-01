package dev.ganainy.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.footy.database.ReminderDatabase
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.DONE_ACTION_FOR_REMINDERS
import dev.ganainy.reminderly.Utils.DONE_ACTION_FOR_REPEATING_REMINDERS
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.Utils.REMINDER_ID
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**this receiver is called when the user presses finish(mark as done) button on a notification*/
class DoneReminderReceiver : BroadcastReceiver() {

    val disposable = CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {


        val reminderId = intent.extras?.get(REMINDER_ID) as Long
        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao


        //get reminder by id and set it to done or just close this notification and reminder will
        // work normally in next repeat if it is repeating alarm
        disposable.add(
            reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { reminder ->

                    //close any ongoing notification/alarm
                MyUtils.closeReminder(reminder,  context)

                    if (reminder.repeat != 0 ){
                        //repeating reminder
                        if (shouldTemporaryCancelAlarmReminder(context)){
                            //we should just stop the alarm service and not delete reminder(already did that by calling MyUtils.closeReminder())
                            MyUtils.showCustomToast(context, R.string.reminder_will_work_next_time)
                        }else{
                            //make the reminder done (won't fire alarm again)
                            //now check do we delete done reminders or keep them
                            //this is one time reminder
                            if (shouldDeleteDoneReminders(context)) {
                                //delete reminder
                                deleteReminder(reminderDatabaseDao, reminder, context)
                            } else {
                                //make the reminder done (won't fire alarm/notification again)
                                markReminderAsDone(reminder, context, reminderDatabaseDao)
                            }

                        }

                    }else
                    {
                        //this is one time reminder
                        if (shouldDeleteDoneReminders(context)) {
                            //delete reminder
                            deleteReminder(reminderDatabaseDao, reminder, context)
                        } else {
                            //make the reminder done (won't fire alarm/notification again)
                            markReminderAsDone(reminder, context, reminderDatabaseDao)
                        }

                    }

            })

    }


    /**
     *  DONE_ACTION_FOR_REMINDERS:Int
     *  this value changes based on user settings
     *  0-> done reminder are saved and can be accessed through menu (default)
     *  1-> done reminders are deleted
     *  */
    private fun shouldDeleteDoneReminders(context: Context) =
        MyUtils.getInt(context, DONE_ACTION_FOR_REMINDERS) == 1


    /**
     *  DONE_ACTION_FOR_REPEATING_REMINDERS:Int
     *  this value changes based on user settings
     *  0-> just cancel this notification and it will work normally in next repeat (default)
     *  1-> end the whole reminder
     *  */
    private fun shouldTemporaryCancelAlarmReminder(context: Context) =
        MyUtils.getInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS) == 0

    private fun deleteReminder(
        reminderDatabaseDao: ReminderDatabaseDao,
        reminder: Reminder,
        context: Context
    ) {
        disposable.add(reminderDatabaseDao.delete(reminder).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(
            {//complete
                MyUtils.showCustomToast(context, R.string.reminder_deleted_can_be_changed_in_settings)
                disposable.clear()
            },
            { error ->
                MyUtils.showCustomToast(context,R.string.something_went_wrong)
                disposable.clear()
            }
        ))
    }

    /**updates reminder in database with isdone=1*/
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
                MyUtils.cancelAlarmManager(reminder.id,context)
                MyUtils.showCustomToast(context, R.string.moved_to_done_list,Toast.LENGTH_LONG)
                disposable.clear()
            },
            { error ->
                MyUtils.showCustomToast(context,R.string.something_went_wrong)
                disposable.clear()
            }
        ))
    }

}
