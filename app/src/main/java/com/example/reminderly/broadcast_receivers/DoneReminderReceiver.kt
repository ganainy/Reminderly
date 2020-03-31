package com.example.reminderly.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**this receiver is called when the user presses finish(mark as done) button on a notification*/
class DoneReminderReceiver : BroadcastReceiver() {

    val disposable=CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.extras?.get("reminderId") as Long


        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao

        //close the notification
        MyUtils.cancelNotification(reminderId,context)

        //get reminder by id and set it to done then update in database
        //this code is recommended to by in a service but is here bc it executes fast and to reduce complexity
        disposable.add( reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe { reminder ->
            reminder.isDone = true
            reminderDatabaseDao.update(reminder).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe (
                {//complete
                    MyUtils.cancelAlarm(reminderId,context)
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
            )

        })

    }

}
