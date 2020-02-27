package com.example.reminderly.ui.calendarActivity

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable

class CalendarViewModel(app:Application,val database:ReminderDatabaseDao) : ViewModel() {


    fun getActiveReminders(): Observable<MutableList<Reminder>> {
        return database.getActiveReminders()
    }
}
