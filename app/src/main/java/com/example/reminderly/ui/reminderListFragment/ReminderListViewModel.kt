package com.example.reminderly.ui.reminderListFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Completable

class ReminderListViewModel(app:Application,val database:ReminderDatabaseDao) : ViewModel() {

    fun updateReminder(reminder: Reminder) :Completable{
      return database.update(reminder)
    }

    fun deleteReminder(reminder: Reminder): Completable {
        return database.delete(reminder)
    }

}
