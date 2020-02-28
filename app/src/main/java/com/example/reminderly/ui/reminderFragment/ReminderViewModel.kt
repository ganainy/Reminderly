package com.example.reminderly.ui.reminderActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import io.reactivex.Completable
import java.util.*

class ReminderViewModel(
    val app: Application,
    val reminder: Reminder,
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {


    fun updateReminderDate(year:Int,month:Int,day:Int){
        reminder.createdAt.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

    }

    fun updateReminderTime(hour:Int,minute:Int){
        reminder.createdAt.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }



    }

    fun updateReminderRepeat(index: Int) {
        reminder.repeat=index
    }

    fun updateReminderPriority(index: Int) {
        reminder.priority= index
    }

    fun updateReminderNotificationType(index: Int) {
        reminder.reminderType= index

    }

    fun updateReminderNotifyAdvAmount(num: Int) {
        reminder.notifyAdvAmount=num
    }

    fun updateReminderNotifyAdvUnit(durationUnit: String) {
        reminder.notifyAdvUnit= when(durationUnit){
            app.getString(R.string.minutes)-> 0
            app.getString(R.string.hours)-> 1
            app.getString(R.string.days)-> 2
            app.getString(R.string.weeks)-> 3
            else -> throw Exception("unknown  type")
        }
    }




    fun updateText(text: String) {
        //set reminder text and save it
        reminder.text=text
    }

    fun saveReminder() : Completable{
        return database.insert(reminder)
    }

    fun resetReminder(){
        //reset default reminder so its params won't be used for future reminders
        reminder.resetToDefaults()
    }


}

