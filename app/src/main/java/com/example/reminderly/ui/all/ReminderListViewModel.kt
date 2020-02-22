package com.example.reminderly.ui.all

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Completable
import io.reactivex.Observable

class ReminderListViewModel(app:Application,val database:ReminderDatabaseDao) : ViewModel() {

    fun updateReminder(reminder: Reminder) :Completable{
        reminder.isFavorite=!reminder.isFavorite
      return database.update(reminder)
    }

    fun getAllReminders() : Observable<MutableList<Reminder>> {
        return database.getAllReminders()
    }
}
