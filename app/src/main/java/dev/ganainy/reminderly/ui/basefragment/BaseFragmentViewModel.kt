package dev.ganainy.reminderly.ui.basefragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class BaseFragmentViewModel(val app: Application, val database: ReminderDatabaseDao) : ViewModel() {


    private val disposable = CompositeDisposable()
    val updatePositionSubject = BehaviorSubject.create<Int>()
    val toastSubject = BehaviorSubject.create<Int>()
    val cancelAlarmSubject = BehaviorSubject.create<Reminder>()
    val addAlarmSubject = BehaviorSubject.create<Reminder>()


    fun updateReminderFavorite(
        reminder: Reminder,
        position: Int
    ) {
        reminder.isFavorite =
            !reminder.isFavorite //change favorite value then update in database
        disposable.add(database.update(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                updatePositionSubject.onNext(position)
            })
    }




    fun deleteReminder(reminder: Reminder, position: Int) {

        disposable.add(database.delete(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                toastSubject.onNext(R.string.reminder_deleted)
                //cancel alarm of this reminder
                cancelAlarmSubject.onNext(reminder)
                updatePositionSubject.onNext(position)
            }, { error ->
                toastSubject.onNext(R.string.reminder_delete_failed)
            })
        )
    }

    /**first check if reminder date after postpone is > current date then update reminder in DB*/
    fun postponeReminder(
        reminder: Reminder,
        day: Int,
        hour: Int,
        minute: Int,
        position: Int
    ) {
        val postponedReminder= MyUtils.postponeReminder(reminder,app, day, hour,minute )
        if (postponedReminder==null){
            //postpone failed do nothing
        }else{
            disposable.add(database.update(postponedReminder).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    // set alarm
                   addAlarmSubject.onNext(reminder)
                   updatePositionSubject.onNext(position)
                    toastSubject.onNext(R.string.reminder_postponed)
                })
        }

    }

    /**make reminder done and update in db && show toast*/
    fun markReminderAsDone(reminder: Reminder, position: Int) {
        reminder.isDone = true
        disposable.add( database.update(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cancelAlarmSubject.onNext(reminder)
                toastSubject.onNext(R.string.marked_as_done)
            })
    }

}