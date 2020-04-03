package com.example.reminderly.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*


@Entity(tableName = "reminder_table")
@Parcelize
data class Reminder(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,//also used to give each reminder unique pending intent request code for alarm ,
    // unique notification id
    var text: String = "",
    var createdAt: Calendar = Calendar.getInstance(),
    var repeat: Int = 0,
    var priority: Int = 0,
    var reminderType: Int = 0,
    var notifyAdvAmount: Int = 0,
    var notifyAdvUnit: Int = 0,
    var isFavorite: Boolean = false,
    var isDone: Boolean = false,
    @Ignore //used to add fake items to reminder list to be shown as Headers
    var header: Int = 0
) : Parcelable {


    fun resetToDefaults() {
        text = ""
        createdAt = Calendar.getInstance()
        repeat = 0
        priority = 0
        reminderType = 0
        notifyAdvAmount = 0
        notifyAdvUnit = 0
        isDone=false
        isFavorite = false
    }

}


/**Header
 * 0-ONCE
 * 1-OVERDUE
 * 2-TODAY
 * 3-UPCOMING
 * */

/**Repeat
 * 0-ONCE
 * 1-HOURLY
 * 2-DAILY
 * 3-WEEKLY
 * 4-MONTHLY
 * 5-YEARLY
 * */

/**Priority
 * 0-LOW
 * 1-MEDIUM
 * 2-HIGH
 * */

/**ReminderType
 * 0-NOTIFICATION
 * 1-ALARM
 * */

/**NotifyAdvUnit
 * 0-MINUTE
 * 1-HOUR
 * 2-DAY
 * 3-WEEK
 * */


