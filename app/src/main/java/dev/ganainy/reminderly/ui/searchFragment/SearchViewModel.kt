package dev.ganainy.reminderly.ui.searchFragment

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


class SearchViewModel(app: Application, val database: ReminderDatabaseDao) : ViewModel() {

    val filteredListSubject = BehaviorSubject.create<MutableList<Reminder>>()
    val toastSubject = BehaviorSubject.create<Int>()
    val emptyListSubject = BehaviorSubject.create<Boolean>()

    @SuppressLint("CheckResult")
    fun searchWithQuery(query: String) {
        database.getActiveRemindersSingle().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapObservable {
                Observable.fromIterable(it)
            }
            .filter { reminder ->
                reminder.text.contains(query)
            }
            .toList()
            .subscribe({ filteredReminderList ->
                if (filteredReminderList.isEmpty()) {
                    emptyListSubject.onNext(true)
                } else {
                    emptyListSubject.onNext(false)
                    filteredListSubject.onNext(filteredReminderList)
                }
            }, {
                toastSubject.onNext(R.string.error_retreiving_reminder)
            })
    }

}
