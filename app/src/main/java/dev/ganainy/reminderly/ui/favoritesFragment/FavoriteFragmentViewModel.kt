package dev.ganainy.reminderly.ui.favoritesFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class FavoriteFragmentViewModel(app: Application, val database: ReminderDatabaseDao) : ViewModel() {

    val reminderListSubject = BehaviorSubject.create<MutableList<Reminder>>()
    val errorSubject = BehaviorSubject.create<String>()
    val emptyListSubject = BehaviorSubject.create<Boolean>()
    private val disposable = CompositeDisposable()


    init {
        disposable.add(
            getFavoriteReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ favoriteReminderList ->
                /**if all favorite reminders are empty show empty layout,else show recycler*/
                if (favoriteReminderList.isEmpty()) {
                    emptyListSubject.onNext(true)
                } else {
                    emptyListSubject.onNext(false)
                    reminderListSubject.onNext(favoriteReminderList)
                }
            }, { error ->
                errorSubject.onNext(error.message.toString())
            })
        )
    }

    private fun getFavoriteReminders(): Observable<MutableList<Reminder>> {
        return database.getFavoriteReminders()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}
