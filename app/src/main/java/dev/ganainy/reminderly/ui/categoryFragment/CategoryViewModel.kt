package dev.ganainy.reminderly.ui.categoryFragment

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.utils.MyUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*

@SuppressLint("CheckResult")
class CategoryViewModel(val app: Application, val database: ReminderDatabaseDao,val dayCalendar:Calendar) : ViewModel() {

    val reminderListSubject = BehaviorSubject.create<MutableList<Reminder>>()
    val toastSubject = BehaviorSubject.create<Int>()
    val emptyListSubject = BehaviorSubject.create<Boolean>()
    val toolbarSubject = BehaviorSubject.create<String>()


    /**get all reminder in certain category(done/upcoming/today/overdue)*/
    fun getSpecificCategoryReminders(
        categoryType: CategoryType
    ) {
        getCategoryReminders(categoryType).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe({ categoryReminders ->

            if (categoryReminders.isEmpty()) {
                emptyListSubject.onNext(true)
            } else {
                emptyListSubject.onNext(false)
                reminderListSubject.onNext(categoryReminders)
            }

        }, { error ->
            toastSubject.onNext(R.string.error_retreiving_reminder)
        })
    }


    private fun getCategoryReminders(categoryType: CategoryType): Observable<MutableList<Reminder>> {
        return when (categoryType) {
            CategoryType.TODAY -> {
                getTodayReminders()
            }
            CategoryType.OVERDUE -> {
                getOverdueReminders()
            }
            CategoryType.UPCOMING -> {
                getUpcomingReminders()
            }
            CategoryType.DONE -> {
                getDoneReminders()
            }
            else -> throw Exception("did you pass certain date category by mistake?")
        }
    }

    /**get reminders at certain date&show that date as fragment title*/
    fun getSpecificDateReminders() {
        database.getInTimeRangeReminders(MyUtils.getStartOfCalendarDay(dayCalendar).timeInMillis,
            MyUtils.getEndOfCalendarDay(dayCalendar).timeInMillis).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ dateReminders ->

                if (dateReminders.isEmpty()) {
                    emptyListSubject.onNext(true)
                } else {
                    toolbarSubject.onNext(
                        app.resources.getString(
                            R.string.date_reminders, (
                                    MyUtils.formatDate(dateReminders[0].createdAt.time))
                        )
                    )

                    emptyListSubject.onNext(false)
                    reminderListSubject.onNext(dateReminders)
                }

            }, { error ->
                toastSubject.onNext(R.string.error_retreiving_reminder)
            })
    }


    private fun getDoneReminders(): Observable<MutableList<Reminder>> {
        return database.getDoneRemindersObservable()
    }

    private fun getUpcomingReminders(): Observable<MutableList<Reminder>> {
        return database.getUpcomingReminders( MyUtils.getEndOfCalendarDay(dayCalendar).timeInMillis)
    }

    private fun getOverdueReminders(): Observable<MutableList<Reminder>> {
        return database.getOverdueReminders(MyUtils.getStartOfCalendarDay(dayCalendar).timeInMillis)
    }

    private fun getTodayReminders(): Observable<MutableList<Reminder>> {
        return database.getInTimeRangeReminders(
            MyUtils.getStartOfCalendarDay(dayCalendar).timeInMillis,
            MyUtils.getEndOfCalendarDay(dayCalendar).timeInMillis
        )
    }

}