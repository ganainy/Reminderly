package com.example.reminderly.ui.mainActivity

import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable

class MainActivityViewModel(val database: ReminderDatabaseDao):ViewModel() {

    fun getAllReminders() : Observable<MutableList<Reminder>> {
        return database.getAllReminders()
    }

    fun getDoneReminders(): Observable<MutableList<Reminder>> {
        return database.getDoneReminders()
    }


}