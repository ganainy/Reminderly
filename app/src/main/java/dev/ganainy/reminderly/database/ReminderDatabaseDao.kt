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
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface ReminderDatabaseDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reminder: Reminder): Single<Long>

    @Update
    fun update(reminder: Reminder) :Completable

    @Delete
    fun delete(reminder: Reminder):Completable

    @Query("DELETE FROM reminder_table")
    fun clearAll()


    @Query("SELECT * FROM reminder_table WHERE id=:id")
    fun getReminderById(id: Long): Maybe<Reminder>

    @Query("DELETE FROM reminder_table WHERE id==:id")
    fun deleteReminderById(id: String?)


    @Query("SELECT * FROM reminder_table WHERE isDone==0")
    fun getActiveRemindersObservable(): Observable<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE isDone==0")
    fun getActiveRemindersSingle(): Single<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE isDone==1")
    fun getDoneRemindersObservable(): Observable<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE isDone==1")
    fun getDoneRemindersSingle(): Single<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE isFavorite==1 AND  isDone==0")
    fun getFavoriteReminders():Observable<MutableList<Reminder>>


    @Query("SELECT * FROM reminder_table WHERE (createdAt>=:nextDayMillis AND  isDone==0)")
    fun getUpcomingReminders(nextDayMillis:Long):Observable<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE (createdAt<:startDayMillis AND  isDone==0)")
    fun getOverdueReminders(startDayMillis: Long): Observable<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE (createdAt>=:startDayMillis AND createdAt<=:endDayMillis AND isDone==0)")
    fun getInTimeRangeReminders(startDayMillis: Long, endDayMillis:Long): Observable<MutableList<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE (createdAt>=:startDayMillis AND createdAt<=:endDayMillis AND isDone==0 AND reminderType==1)")
    fun getInTimeRangeAlarmReminders(startDayMillis: Long, endDayMillis:Long): Single<MutableList<Reminder>>

}

