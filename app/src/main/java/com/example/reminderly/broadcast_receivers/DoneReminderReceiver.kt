package com.example.reminderly.broadcast_receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class DoneReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val reminderId = intent.extras?.get("reminderId") as? Long
        val mNotifyManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

        /**get reminder by id and set it to done*/
        val reminderDatabaseDao = ReminderDatabase.getInstance(context).reminderDatabaseDao
        reminderId?.let { reminderId ->
            reminderDatabaseDao.getReminderById(reminderId).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe { reminder ->
                reminder.isDone = true
                mNotifyManager.cancel(reminder.requestCode)
                reminderDatabaseDao.update(reminder).subscribeOn(Schedulers.io()).observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe(object : CompletableObserver {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                )
            }
        }

    }


}
