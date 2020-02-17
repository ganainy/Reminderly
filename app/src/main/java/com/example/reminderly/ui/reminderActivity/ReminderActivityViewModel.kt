package com.example.reminderly.ui.reminderActivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.reminderly.R
import com.example.reminderly.model.*
import java.util.*

class ReminderActivityViewModel(val app:Application) : AndroidViewModel(app) {


    val tempReminderList= mutableListOf<Reminder>()

      val defaultReminder by lazy {
         Reminder() }

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
        defaultReminder.repeat= when(index){
            0->Repeat.ONCE
            1->Repeat.HOURLY
            2->Repeat.DAILY
            3->Repeat.WEEKLY
            4->Repeat.MONTHLY
            5->Repeat.YEARLY
            else -> throw Exception("unknown  type")
        }
    }

    fun updateReminderPriority(index: Int) {
        defaultReminder.priority= when(index){
            0->Priority.LOW
            1->Priority.MEDIUM
            2->Priority.HIGH
            else -> throw Exception("unknown  type")
        }
    }

    fun updateReminderNotificationType(index: Int) {
        defaultReminder.repeatType= when(index){
            0-> RepeatType.NOTIFICATION
            1->RepeatType.ALARM
            else -> throw Exception("unknown  type")
        }
    }

    fun updateReminderNotifyAdvAmount(num: Int) {
        defaultReminder.notifyAdvAmount=num
    }

    fun updateReminderNotifyAdvUnit(durationUnit: String) {
        defaultReminder.notifyAdvUnit= when(durationUnit){
            app.getString(R.string.minutes)-> NotifyAdvUnit.MINUTE
            app.getString(R.string.hours)-> NotifyAdvUnit.HOUR
            app.getString(R.string.days)-> NotifyAdvUnit.DAY
            app.getString(R.string.weeks)-> NotifyAdvUnit.WEEK
            else -> throw Exception("unknown  type")
        }
    }



    fun updateReminderClickableString(clickableText: String) {
        defaultReminder.clickableStrings.add(clickableText)
        Log.d("DebugTag", "updateReminderClickableString: "+defaultReminder.clickableStrings)
    }


    fun saveReminder(text: String) {
        //set reminder text and save it
        defaultReminder.text=text
        tempReminderList.add(defaultReminder)
        //reset default reminder so its params won't be used for future reminders
        defaultReminder.resetToDefaults()

    }


}

