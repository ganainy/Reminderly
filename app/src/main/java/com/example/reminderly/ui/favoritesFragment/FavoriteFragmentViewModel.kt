package com.example.reminderly.ui.favoritesFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import io.reactivex.Observable

class FavoriteFragmentViewModel(app:Application, val database:ReminderDatabaseDao) : ViewModel() {

    fun getFavoriteReminders(): Observable<MutableList<Reminder>> {
        return database.getFavoriteReminders()
    }

}
