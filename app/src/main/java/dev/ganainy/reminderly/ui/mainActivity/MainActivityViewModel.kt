package dev.ganainy.reminderly.ui.mainActivity

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable
import java.util.*

class MainActivityViewModel(app:Application,val database: ReminderDatabaseDao):ViewModel() {

    /**millis of the end of today so we can get any reminders after that (upcoming reminders)*/
    private val nextDayMillis:Long
        get() {
          return  Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY,23)
                set(Calendar.MINUTE,59)
                set(Calendar.SECOND,59)
              set(Calendar.MILLISECOND,999)

            }.timeInMillis
        }


    /**millis of the begging of today so we can get any reminders before that (overdue reminders)*/
    private val todayMillis:Long
        get() {
            return  Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY,0)
                set(Calendar.MINUTE,0)
                set(Calendar.SECOND,0)
                set(Calendar.MILLISECOND,0)

            }.timeInMillis
        }

    fun getDoneReminders(): Observable<MutableList<Reminder>> {
        return database.getDoneRemindersObservable()
    }

    fun getUpcomingReminders(): Observable<MutableList<Reminder>> {
        return database.getUpcomingReminders(nextDayMillis)
    }

    fun getOverdueReminders(): Observable<MutableList<Reminder>> {
        return database.getOverdueReminders(todayMillis)
    }

    fun getTodayReminders(): Observable<MutableList<Reminder>> {
        return database.getInTimeRangeReminders(todayMillis,nextDayMillis)
    }




}