package dev.ganainy.reminderly.ui.calendarActivity

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.prolificinteractive.materialcalendarview.CalendarDay
import dev.ganainy.reminderly.utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class CalendarViewModel(app: Application, val database: ReminderDatabaseDao) : ViewModel() {

    private lateinit var mCalendarDays: MutableList<CalendarDay>
    val disposable = CompositeDisposable()
    val activeReminderListSubject = BehaviorSubject.create<MutableList<CalendarDay>>()
    val errorSubject = BehaviorSubject.create<String>()
    val fragmentCalendarSubject = BehaviorSubject.create<Pair<String, Calendar>>()


    @SuppressLint("CheckResult")
    fun getDaysContainingReminders() {

        disposable.add(database.getActiveRemindersObservable().subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).flatMap { activeReminderList ->
            getCalendarDaysFromReminderList(activeReminderList)
        }.subscribe({ calendarDays ->
            activeReminderListSubject.onNext(calendarDays)
            mCalendarDays = calendarDays
            disposable.clear()
        }, { error ->
            errorSubject.onNext(error.message.toString())
            disposable.clear()
        })
        )

    }


    fun getCalendarDaysFromReminderList(activeReminderList: MutableList<Reminder>)
            : ObservableSource<MutableList<CalendarDay>> {
        val calendarDays = mutableListOf<CalendarDay>()
        for (reminder in activeReminderList) {
            calendarDays.add(
                CalendarDay.from(
                    reminder.createdAt.get(Calendar.YEAR),
                    reminder.createdAt.get(Calendar.MONTH) + 1,
                    reminder.createdAt.get(Calendar.DAY_OF_MONTH)
                )
            )
        }
        return Observable.just(calendarDays)
    }


    fun onCalendarDayClicked(clickedCalendarDay: CalendarDay) {

        if (::mCalendarDays.isInitialized) {
            for (calendarDay in mCalendarDays) {
                if (clickedCalendarDay == calendarDay) {
                    /**if clicked calendar in calendarDays this means clicked date has reminders
                     * so open category fragment and show them*/
                    fragmentCalendarSubject.onNext(
                        Pair(
                            CATEGORY_FRAGMENT,
                            MyUtils.calendarDayToCalendar(clickedCalendarDay)
                        )
                    )
                    return
                }
            }
            /**if clicked calendar not in calendarDays this means clicked date has no reminders
             * so open new reminder fragment with this date*/
            fragmentCalendarSubject.onNext(
                Pair(
                    REMINDER_FRAGMENT,
                    MyUtils.calendarDayToCalendar(clickedCalendarDay)
                )
            )
        }

    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
