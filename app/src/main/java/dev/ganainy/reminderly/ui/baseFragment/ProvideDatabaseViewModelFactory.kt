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

package dev.ganainy.reminderly.ui.baseFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabaseDao

/**
 * Simple ViewModel factory that provides the database and context to the ViewModel.
 */
class ProvideDatabaseViewModelFactory(
    private val application: Application,
    private val database: ReminderDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
       try {
           return modelClass.getConstructor(Application::class.java,ReminderDatabaseDao::class.java).newInstance(application,database)
       }catch (e:InstantiationException){
           throw Exception("InstantiationException, cannot create instance of $modelClass")
       }catch (e:IllegalAccessException){
           throw Exception("IllegalAccessException, cannot create instance of $modelClass")
       }
    }
}
