package dev.ganainy.reminderly.ui.baseFragment

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.utils.MyUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class BaseFragmentViewModel(val app: Application, val database: ReminderDatabaseDao) : ViewModel() {

    val toastSubject = PublishSubject.create<@StringRes Int>()
    val addAlarmSubject = PublishSubject.create<Reminder>()
    val cancelAlarmSubject = PublishSubject.create<Reminder>()
    val cancelNotificationSubject = PublishSubject.create<Reminder>()

    /**change reminder favorite value then update in database*/
    fun updateReminderFavorite(
        reminder: Reminder
    ): Completable {
        reminder.isFavorite = !reminder.isFavorite
        return database.update(reminder)
    }


    /**first check if reminder date after postpone is > current date then update reminder, its alarm*/
    fun postponeReminder(
        reminder: Reminder,
        day: Int,
        hour: Int,
        minute: Int
    ): Completable {
        val postponedReminder = MyUtils.postponeReminder(reminder, app, day, hour, minute)
        return if (postponedReminder == null) {
            //postpone failed
            Completable.error(Throwable("Postpone failed due to wrong selected date"))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                toastSubject.onNext(R.string.postpone_failed)
            }
        } else {
            database.update(postponedReminder)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //cancel any ongoing notification
                    cancelNotificationSubject.onNext(reminder)
                    // cancel existing alarm & set new alarm
                    cancelAlarmSubject.onNext(reminder)
                    addAlarmSubject.onNext(reminder)
                  //  updatePositionSubject.onNext(position)
                    toastSubject.onNext(R.string.reminder_postponed)
                }
                .doOnError {
                    toastSubject.onNext(R.string.something_went_wrong)
                }
        }

    }

    /**make reminder done and update in db or delete reminder depending on settings&& show
     * adjacent toast*/
    fun handleReminderDoneBehaviour(reminder: Reminder): Completable {
        return if (MyUtils.shouldDeleteDoneReminders(app)) {
            deleteDoneReminder(reminder)
        } else {
            markReminderAsDone(reminder)
        }
    }

    private fun markReminderAsDone(reminder: Reminder): Completable {
        reminder.isDone = true
        return database.update(reminder).observeOn(AndroidSchedulers.mainThread()).doOnComplete {
            toastSubject.onNext(R.string.marked_as_done)
            cancelAlarmSubject.onNext(reminder)
            //cancel any ongoing notification
            cancelNotificationSubject.onNext(reminder)
        }.doOnError {
            toastSubject.onNext(R.string.something_went_wrong)
        }
    }

    private fun deleteDoneReminder(reminder: Reminder): Completable {
        return database.delete(reminder).observeOn(AndroidSchedulers.mainThread()).doOnComplete {
            toastSubject.onNext(R.string.reminder_deleted_can_be_changed_in_settings)
            cancelAlarmSubject.onNext(reminder)
            //cancel any ongoing notification
            cancelNotificationSubject.onNext(reminder)
        }
            .doOnError {
                toastSubject.onNext(R.string.something_went_wrong)
            }
    }


    fun deleteReminder(reminder: Reminder): Completable {
       return database.delete(reminder)
            .observeOn(AndroidSchedulers.mainThread())
           .doOnComplete {
               toastSubject.onNext(R.string.reminder_deleted)
               //cancel alarm of this reminder
               cancelAlarmSubject.onNext(reminder)
               //cancel any ongoing notification
               cancelNotificationSubject.onNext(reminder)
           }
           .doOnError {error ->
               toastSubject.onNext(R.string.reminder_delete_failed)
           }
    }

}
