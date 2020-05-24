package dev.ganainy.reminderly.ui.reminderActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class ReminderViewModel(
    val app: Application,
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {

    val reminderSubject=BehaviorSubject.create<Reminder>()

     var reminder=Reminder()

    fun updateReminder(){
        reminderSubject.onNext(reminder)
    }


    fun saveReminder(): Single<Long> {
        return database.insert(reminder).subscribeOn(Schedulers.io())
    }

    fun getInTimeRangeAlarmReminders(startMillis:Long,endMillis:Long): Observable<MutableList<Reminder>> {
        return database.getInTimeRangeAlarmReminders(startMillis,endMillis).subscribeOn(Schedulers.io())
    }


    /**check if there is another reminder from createdAt -3 min until createdAt +3min*/
    fun isSameTimeOfAnotherAlarm(): Observable<MutableList<Reminder>> {

        return getInTimeRangeAlarmReminders(
            reminder.createdAt.timeInMillis-(1000*60*3), //3 minutes
            reminder.createdAt.timeInMillis+(1000*60*3) //3 minutes
        )
    }


}

