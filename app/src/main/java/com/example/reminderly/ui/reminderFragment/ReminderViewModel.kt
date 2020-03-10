package com.example.reminderly.ui.reminderActivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class ReminderViewModel(
    val app: Application,
    val mReminder: Reminder,
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {


    fun updateReminderDate(year:Int,month:Int,day:Int){
        mReminder.createdAt.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

    }

    fun updateReminderTime(hour:Int,minute:Int){
        mReminder.createdAt.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }



    }

    fun updateReminderRepeat(index: Int) {
        mReminder.repeat=index
    }

    fun updateReminderPriority(index: Int) {
        mReminder.priority= index
    }

    fun updateReminderNotificationType(index: Int) {
        mReminder.reminderType= index

    }

    fun updateReminderNotifyAdvAmount(num: Int) {
        mReminder.notifyAdvAmount=num
    }

    fun updateReminderNotifyAdvUnit(durationUnit: String) {
        mReminder.notifyAdvUnit= when(durationUnit){
            app.getString(R.string.minutes)-> 0
            app.getString(R.string.hours)-> 1
            app.getString(R.string.days)-> 2
            app.getString(R.string.weeks)-> 3
            else -> throw Exception("unknown  type")
        }
    }




    fun updateText(text: String) {
        //set reminder text and save it
        mReminder.text=text
    }

    fun saveReminder() : Single<Long> {
        return database.insert(mReminder)
    }

    fun getReminder() : Reminder{
        return mReminder
    }

    fun resetReminder(){
        //reset default reminder so its params won't be used for future reminders
        mReminder.resetToDefaults()
    }

    fun updateReminderRequstCode(pendingIntentRequestCode: Int) {
        mReminder.requestCode=pendingIntentRequestCode
    }


}

