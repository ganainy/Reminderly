package com.example.reminderly.ui.reminderActivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import io.reactivex.Completable
import java.util.*

class ReminderActivityViewModel(
    val app: Application,
    private val database: ReminderDatabaseDao
) : AndroidViewModel(app) {



      private val defaultReminder by lazy {
          Reminder()
      }

    fun updateReminderDate(year:Int,month:Int,day:Int){
        defaultReminder.createdAt.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

    }

    fun updateReminderTime(hour:Int,minute:Int){
        defaultReminder.createdAt.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }



    }

    fun updateReminderRepeat(index: Int) {
        defaultReminder.repeat=index
    }

    fun updateReminderPriority(index: Int) {
        defaultReminder.priority= index
    }

    fun updateReminderNotificationType(index: Int) {
        defaultReminder.reminderType= index

    }

    fun updateReminderNotifyAdvAmount(num: Int) {
        defaultReminder.notifyAdvAmount=num
    }

    fun updateReminderNotifyAdvUnit(durationUnit: String) {
        defaultReminder.notifyAdvUnit= when(durationUnit){
            app.getString(R.string.minutes)-> 0
            app.getString(R.string.hours)-> 1
            app.getString(R.string.days)-> 2
            app.getString(R.string.weeks)-> 3
            else -> throw Exception("unknown  type")
        }
    }



    fun updateReminderClickableString(clickableText: String) {
        defaultReminder.clickableStrings.add(clickableText)
        Log.d("DebugTag", "updateReminderClickableString: "+defaultReminder.clickableStrings)
    }


    fun updateText(text: String) {
        //set reminder text and save it
        defaultReminder.text=text
    }

    fun saveReminder() : Completable{
        return database.insert(defaultReminder)
    }

    fun resetReminder(){
        //reset default reminder so its params won't be used for future reminders
        defaultReminder.resetToDefaults()
    }


}

