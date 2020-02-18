/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.footy.database

import androidx.room.*
import com.example.reminderly.model.Reminder
import io.reactivex.Completable

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface ReminderDatabaseDao {


    @Insert
    fun insert(reminder: Reminder): Completable

    @Update
    fun update(reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)

    @Query("DELETE FROM reminder_table")
    fun clearAll()


    @Query("SELECT * FROM reminder_table WHERE id=:id")
    fun getReminderByID(id: Int): Reminder

    @Query("DELETE FROM reminder_table where id==:id")
    fun deleteReminderById(id: String?)


    @Query("SELECT * FROM reminder_table ")
    fun getAllReminders(): List<Reminder>?

    @Query("SELECT * FROM reminder_table WHERE isFavorite==1")
    fun getFavoriteReminders():List<Reminder>?

}

