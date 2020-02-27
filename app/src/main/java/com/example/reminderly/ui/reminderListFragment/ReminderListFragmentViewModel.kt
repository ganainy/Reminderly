package com.example.reminderly.ui.reminderListFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable

class ReminderListFragmentViewModel(app:Application, val database:ReminderDatabaseDao) : ViewModel() {

    fun getAllReminders() : Observable<MutableList<Reminder>> {
        return database.getActiveReminders()
    }




}
