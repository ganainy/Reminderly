package com.example.reminderly.ui.reminderActivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class ReminderViewModel(
    val app: Application,
    val mReminder: Reminder,//useless
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {



    fun saveReminder(reminder: Reminder): Single<Long> {
        return database.insert(reminder)
    }

}

