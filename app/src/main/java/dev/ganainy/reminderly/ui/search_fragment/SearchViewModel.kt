package dev.ganainy.reminderly.ui.search_fragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable

class SearchViewModel(app:Application,val database:ReminderDatabaseDao) : ViewModel() {
    fun getAllReminders() : Observable<MutableList<Reminder>> {
        return database.getActiveReminders()
    }
}
