package com.example.reminderly.ui.search_fragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable

class SearchViewModel(app:Application,val database:ReminderDatabaseDao) : ViewModel() {
    fun getAllReminders() : Observable<MutableList<Reminder>> {
        return database.getActiveReminders()
    }
}
