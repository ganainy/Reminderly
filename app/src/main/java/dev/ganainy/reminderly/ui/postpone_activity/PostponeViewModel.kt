package dev.ganainy.reminderly.ui.postpone_activity

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Completable
import io.reactivex.Maybe


class PostponeViewModel(app: Application, val database: ReminderDatabaseDao) : ViewModel() {


    fun getReminderById(id:Long): Maybe<Reminder> {
        return database.getReminderById(id)
    }

    fun updateReminder(reminder: Reminder) : Completable {
        return database.update(reminder)
    }
}
