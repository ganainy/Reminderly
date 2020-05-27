package dev.ganainy.reminderly.ui.reminderListFragment

import android.annotation.SuppressLint
import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class ReminderListFragmentViewModel(app: Application, val database: ReminderDatabaseDao) :
    ViewModel() {


    private val overdueReminders = mutableListOf<Reminder>()
    private val todayReminders = mutableListOf<Reminder>()
    private val upcomingReminders = mutableListOf<Reminder>()
    private val reminderListWithHeaders = mutableListOf<Reminder>()



    val reminderListSubject = BehaviorSubject.create<MutableList<Reminder>>()
    val errorSubject = BehaviorSubject.create<String>()
    val emptyListSubject = BehaviorSubject.create<Boolean>()

    /**get all active reminders(not done) from db and order them & add headers
     * (overdue-today-upcoming) & ads */
    @SuppressLint("CheckResult")
    fun getAllRemindersFormatted() {
            database.getActiveRemindersObservable().subscribeOn(Schedulers.io())
                .subscribe({ reminderList ->

                    overdueReminders.clear()
                    todayReminders.clear()
                    upcomingReminders.clear()
                    reminderListWithHeaders.clear()


                    for (reminder in reminderList) {
                        val currentCalendar = Calendar.getInstance()
                        when {
                            DateUtils.isToday(reminder.createdAt.timeInMillis) -> {
                                todayReminders.add(reminder)
                            }
                            reminder.createdAt.before(currentCalendar) -> {
                                overdueReminders.add(reminder)
                            }
                            else -> {
                                upcomingReminders.add(reminder)
                            }
                        }
                    }

                    if (overdueReminders.isNotEmpty()) {
                        reminderListWithHeaders.add(Reminder(header = 1))//add empty reminder with header value that will be used as header in recycler
                        for (reminder in overdueReminders) {
                            reminderListWithHeaders.add(reminder)
                        }
                    }
                    if (todayReminders.isNotEmpty()) {
                        reminderListWithHeaders.add(Reminder(header = 2))//add empty reminder with header value that will be used as header in recycler
                        for (reminder in todayReminders) {
                            reminderListWithHeaders.add(reminder)
                        }
                    }
                    if (upcomingReminders.isNotEmpty()) {
                        reminderListWithHeaders.add(Reminder(header = 3))//add empty reminder with header value that will be used as header in recycler
                        for (reminder in upcomingReminders) {
                            reminderListWithHeaders.add(reminder)
                        }
                    }

                    addAdItemToList(reminderListWithHeaders)

                    if (reminderListWithHeaders.size == 0) {
                        emptyListSubject.onNext(true)
                    } else {
                        emptyListSubject.onNext(false)
                        reminderListSubject.onNext(reminderListWithHeaders)
                    }

                }, { error ->
                    errorSubject.onNext(error.message.toString())
                })


    }


    /**add ad item after every 8 reminders*/
    private fun addAdItemToList(reminderListWithHeaders: MutableList<Reminder>) {

        for (i in 1 until reminderListWithHeaders.size) {
            if (i % 8 == 0)
                reminderListWithHeaders.add(
                    i,
                    Reminder(header = 4)
                )//add empty reminder with header value that will be used as AD in recycler
        }

    }


}
