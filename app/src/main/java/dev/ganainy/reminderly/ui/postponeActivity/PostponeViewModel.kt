package dev.ganainy.reminderly.ui.postponeActivity

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.utils.MyUtils
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class PostponeViewModel(val app: Application, val database: ReminderDatabaseDao) : ViewModel() {


    val errorSubject=BehaviorSubject.create<Boolean>()
    val navigateBackSubject=BehaviorSubject.create<Boolean>()
    val toastSubject=BehaviorSubject.create<Int>()


    fun getReminderById(id:Long): Maybe<Reminder> {
        return database.getReminderById(id)
    }


    @SuppressLint("CheckResult")
    fun postponeReminder(
        reminder: Reminder,
        dayPicked: Int,
        hourPicked: Int,
        minPicked: Int
    ) {
        val postponedReminder = MyUtils.postponeReminder(
            reminder,
            app,
            dayPicked,
            hourPicked,
            minPicked
        )

        if (postponedReminder == null) {
            //postpone failed show error
            toastSubject.onNext(R.string.must_be_upcoming_date)
            errorSubject.onNext(true)
        } else {
            errorSubject.onNext(false)
            //update reminder with new postponed date
            database.update(postponedReminder).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).subscribe {
                // set alarm
                MyUtils.cancelAlarmManager(reminder,app )
                MyUtils.addAlarmManager(
                    reminder,
                    app
                )

               toastSubject.onNext(R.string.reminder_postponed)

               navigateBackSubject.onNext(true)
            }
        }
    }

    fun stopReminderUi(reminder: Reminder) {
        MyUtils.closeReminder(reminder, app)
    }
}
