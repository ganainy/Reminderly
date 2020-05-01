package com.example.reminderly.ui.reminderActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.Single

class ReminderViewModel(
    val app: Application,
    val mReminder: Reminder,//useless
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {

     var reminder=Reminder()


    fun saveReminder(reminder: Reminder): Single<Long> {
        return database.insert(reminder)
    }

    fun getInTimeRangeAlarmReminders(startMillis:Long,endMillis:Long): Observable<MutableList<Reminder>> {
        return database.getInTimeRangeAlarmReminders(startMillis,endMillis)
    }

}

