package dev.ganainy.reminderly.ui.settingsFragment

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class SettingsViewModel(val app:Application,val database: ReminderDatabaseDao) :ViewModel(){

    val reminderListSubject = BehaviorSubject.create<MutableList<Reminder>>()
    val toastSubject = BehaviorSubject.create<Int>()
    val emptyListSubject = BehaviorSubject.create<Boolean>()

    @SuppressLint("CheckResult")
    fun deleteExistingDoneReminders() {
        database.getDoneRemindersSingle().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapObservable {
                Observable.fromIterable(it)
            }
            .map {
                deleteReminder(it)
            }
    }

    @SuppressLint("CheckResult")
    private fun deleteReminder(reminderToDelete: Reminder) {
        database.delete(reminderToDelete).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).doOnError {
                toastSubject.onNext(R.string.something_went_wrong) }
    }

}