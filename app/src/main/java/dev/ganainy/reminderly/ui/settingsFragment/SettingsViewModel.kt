package dev.ganainy.reminderly.ui.settingsFragment

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.models.DndPeriod
import dev.ganainy.reminderly.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

@SuppressLint("CheckResult")
class SettingsViewModel(val app: Application, val database: ReminderDatabaseDao) : ViewModel() {

    val toastSubject = BehaviorSubject.create<Int>()
    val dndSubject = BehaviorSubject.create<DndPeriod>()
    val doneBehaviourSummarySubject = BehaviorSubject.create<String>()
    val doneBehaviourRepeatingTasksSummarySubject = BehaviorSubject.create<String>()
    val nightModeUpdateSubject = PublishSubject.create<Boolean>()
    val updateDontDisturbSwitchSubject = BehaviorSubject.create<Boolean>()
    val updatePersistentNotificationSwitchSubject = BehaviorSubject.create<Boolean>()

    init {
        //any time this subject is changes it will update saved values of dnd time in preferences
        dndSubject.doOnNext { updateDndInPreferences(it) }
    }

    private fun updateDndInPreferences(dndPeriod: DndPeriod) {
        //save selected start time to shared pref
        MyUtils.putInt(app, DONT_DISTURB_START_HOURS, dndPeriod.startHour)
        MyUtils.putInt(app, DONT_DISTURB_START_MINUTES, dndPeriod.startMinute)

        //save selected end time to shared pref
        MyUtils.putInt(app, DONT_DISTURB_END_HOURS, dndPeriod.endHour)
        MyUtils.putInt(app, DONT_DISTURB_END_MINUTES, dndPeriod.endMinute)
    }

    private val dndTime = DndPeriod()

    fun deleteExistingDoneReminders() {
      database.getDoneRemindersSingle().subscribeOn(Schedulers.io())
            .flatMapObservable {
               Observable.fromIterable(it)
            }
            .map {
                  deleteReminder(it)
                it
            }
          .toList()
          .subscribe ()
    }

    private fun deleteReminder(reminderToDelete: Reminder) {
        database.delete(reminderToDelete).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                 toastSubject.onNext(R.string.something_went_wrong)
            }.subscribe()
    }

    fun getCurrentDndPeriod() {
        dndTime.startMinute = MyUtils.getInt(app, DONT_DISTURB_START_MINUTES)
        dndTime.startHour = MyUtils.getInt(app, DONT_DISTURB_START_HOURS)
        dndTime.endHour = MyUtils.getInt(app, DONT_DISTURB_END_HOURS)
        dndTime.endMinute = MyUtils.getInt(app, DONT_DISTURB_END_MINUTES)
        dndSubject.onNext(dndTime)
    }

    fun updateDndEnd(hour: Int, min: Int) {
        if (validateEndTime(hour, min)) {
            dndTime.endHour = hour
            dndTime.endMinute = min
            dndSubject.onNext(dndTime)
        }
    }


    /**check that the selected end time is higher than the selected/saved start time*/
    private fun validateEndTime(hour: Int, min: Int): Boolean {
        when {
            hour > MyUtils.getInt(app, DONT_DISTURB_START_HOURS) -> {
                return true
            }
            hour == MyUtils.getInt(app, DONT_DISTURB_START_HOURS) -> {
                when {
                    min > MyUtils.getInt(app, DONT_DISTURB_START_MINUTES) -> {
                        return true
                    }
                    else -> {
                        toastSubject.onNext(R.string.wrong_dnd_period)
                    }
                }
            }
            else -> {
                toastSubject.onNext(R.string.wrong_dnd_period)
            }
        }
        return false
    }

    /**check if dnd selected start time is valid and save it in preferences*/
    fun updateDndStart(hour: Int, min: Int) {
        if (validateStartTime(hour, min)) {
            dndTime.startHour = hour
            dndTime.startMinute = min
            dndSubject.onNext(dndTime)
        }
    }


    /**check that the selected start time is less than the selected/saved end time*/
    private fun validateStartTime(hour: Int, min: Int): Boolean {
        when {
            hour < MyUtils.getInt(app, DONT_DISTURB_END_HOURS) -> {
                return true
            }
            hour == MyUtils.getInt(app, DONT_DISTURB_END_HOURS) -> {
                when {
                    min < MyUtils.getInt(app, DONT_DISTURB_END_MINUTES) -> {
                        return true
                    }
                    else -> {
                        toastSubject.onNext(R.string.wrong_dnd_period)
                    }
                }
            }
            else -> {
                toastSubject.onNext(R.string.wrong_dnd_period)
            }
        }
        return false
    }

    //update shared pref value , setting summary based on user selection
    fun updateDoneBehaviourSharedPref(index: Int) {
        when (index) {
            0 -> {
                MyUtils.putInt(app, DONE_ACTION_FOR_REMINDERS, 0)
            }
            1 -> {
                MyUtils.putInt(app, DONE_ACTION_FOR_REMINDERS, 1)
                deleteExistingDoneReminders()
            }
        }
    }

    //update shared pref value , setting summary based on user selection
    fun updateDoneBehaviourRepeatingTasksSharedPref(index: Int) {
        when (index) {
            0 -> {
                MyUtils.putInt(app, DONE_ACTION_FOR_REPEATING_REMINDERS, 0)
            }
            1 -> {
                MyUtils.putInt(app, DONE_ACTION_FOR_REPEATING_REMINDERS, 1)
            }
        }
    }

    fun observeDoneBehaviourSharedPref() {

        val doneBehaviourSharedPref: com.f2prateek.rx.preferences2.Preference<Int> =
            MyUtils.getRxPreferences(app).getInteger(DONE_ACTION_FOR_REMINDERS, -1)

        doneBehaviourSharedPref.asObservable().subscribe {
            when (it) {
                0 -> {
                    doneBehaviourSummarySubject.onNext(
                        MyUtils.getStringFromResourceArray(
                            app,
                            R.array.done_behaviour_list, 0
                        )
                    )
                }
                1 -> {
                    doneBehaviourSummarySubject.onNext(
                        MyUtils.getStringFromResourceArray(
                            app,
                            R.array.done_behaviour_list, 1
                        )
                    )
                }
            }
        }

    }


    fun observeDoneBehaviourRepeatingTasksSharedPref() {

        val doneBehaviourRepeatingTasksSharedPref: com.f2prateek.rx.preferences2.Preference<Int> =
            MyUtils.getRxPreferences(app).getInteger(DONE_ACTION_FOR_REPEATING_REMINDERS, -1)

        doneBehaviourRepeatingTasksSharedPref.asObservable().subscribe {
            when (it) {
                0 -> {
                    doneBehaviourRepeatingTasksSummarySubject.onNext(
                        MyUtils.getStringFromResourceArray(
                            app,
                            R.array.done_behaviour_for_recurring_tasks_list, 0
                        )
                    )
                }
                1 -> {
                    doneBehaviourRepeatingTasksSummarySubject.onNext(
                        MyUtils.getStringFromResourceArray(
                            app,
                            R.array.done_behaviour_for_recurring_tasks_list, 1
                        )
                    )
                }
            }

        }

    }

    fun updateNightMode(isSwitchChecked: Boolean) {
        if (isSwitchChecked) {
            MyUtils.putInt(app, NIGHT_MODE_ENABLED, 1)
            nightModeUpdateSubject.onNext(true)
        } else {
            MyUtils.putInt(app, NIGHT_MODE_ENABLED, 0)
            nightModeUpdateSubject.onNext(true)
        }
    }

    fun updateDontDisturb(isDontDisturbEnabled: Boolean) {
        if (isDontDisturbEnabled) {
            MyUtils.putInt(app, DND_OPTION_ENABLED, 1)
            updateDontDisturbSwitchSubject.onNext(true)
        } else {
            MyUtils.putInt(app, DND_OPTION_ENABLED, 0)
            updateDontDisturbSwitchSubject.onNext(false)
        }
    }

    fun observeAllowPersistentNotificationSharedPref() {
        val allowPersistentNotification: com.f2prateek.rx.preferences2.Preference<Int> =
            MyUtils.getRxPreferences(app).getInteger(ALLOW_PERSISTENT_NOTIFICATION, -1)

        allowPersistentNotification.asObservable().subscribe {
            Timber.d("DebugTag, SettingsFragment->setupSelections: ${it}")

            when (it) {
                0 -> updatePersistentNotificationSwitchSubject.onNext(true)
                1 -> updatePersistentNotificationSwitchSubject.onNext(false)
            }
        }

    }


}