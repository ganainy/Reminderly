package com.example.reminderly.ui.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabaseDao

class MainActivityViewModelFactory(
    private val database: ReminderDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(
                database
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
