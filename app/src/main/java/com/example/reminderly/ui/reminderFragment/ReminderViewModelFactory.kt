/*
 *  Copyright 2018, The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.reminderly.ui.reminderFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.reminderActivity.ReminderViewModel

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class ReminderViewModelFactory(
    private val app: Application,
    private val reminder: Reminder,
    private val database: ReminderDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            return ReminderViewModel(
                app,
                reminder,
                database
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
