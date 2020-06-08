package dev.ganainy.reminderly.ui.reminderActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.footy.database.ReminderDatabaseDao
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*

class ReminderViewModel(
    val app: Application,
    val database: ReminderDatabaseDao
) : AndroidViewModel(app) {

    /*TODO fix wierd reminder adapter issue and edit reminder
    *  جرب ان اعمل رفرش للمكان بس لما امسح او اعمل ايديت واخلي يجيب قايمه مره واحده بس*/

    val cancelAlarmSubject = BehaviorSubject.create<Reminder>()
    val addAlarmSubject = BehaviorSubject.create<Reminder>()
    val toastSubject = BehaviorSubject.create<Int>()
    val backSubject = BehaviorSubject.create<Boolean>()
    val disposable = CompositeDisposable()

    var reminder = Reminder()

    val reminderSubject = BehaviorSubject.create<Reminder>()


    init {
        //pass initial reminder on viewmodel creation to fill views until user changes reminder
        reminderSubject.onNext(reminder)
    }


    fun updateReminder() {
        reminderSubject.onNext(reminder)
    }


    fun saveReminder(): Single<Long> {
        return database.insert(reminder).subscribeOn(Schedulers.io())
    }

    fun getInTimeRangeAlarmReminders(
        startMillis: Long,
        endMillis: Long
    ): Single<MutableList<Reminder>> {
        return database.getInTimeRangeAlarmReminders(startMillis, endMillis)
            .subscribeOn(Schedulers.io())
    }


    /**check if there is another reminder from createdAt -3 min until createdAt +3min*/
    fun isSameTimeOfAnotherAlarm(): Single<MutableList<Reminder>> {

        return getInTimeRangeAlarmReminders(
            reminder.createdAt.timeInMillis - (1000 * 60 * 3), //3 minutes
            reminder.createdAt.timeInMillis + (1000 * 60 * 3) //3 minutes
        )
    }


    fun handleSaveButton() {

       if (reminder.text.isBlank()) {
           toastSubject.onNext(R.string.text_empty)
            return
        }
       else if(reminder.createdAt.timeInMillis <= Calendar.getInstance().timeInMillis){
           toastSubject.onNext(R.string.old_date_error)
       return
   }

        disposable.add(isSameTimeOfAnotherAlarm()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {sameTimeAlarms ->
                Timber.d("DebugTag, ReminderViewModel->handleSaveButton: ")

                if (sameTimeAlarms.size>0){
                    //there is another reminders near this reminder time so don't allow operation
                    toastSubject.onNext(R.string.another_reminder_in_proximity)
                }else{
                saveReminder().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { reminderId ->
                            reminder.id = reminderId
                            //this reminder could be an update for existing reminder so we cancel any ongoing alarms
                            cancelAlarmSubject.onNext(reminder)
                            // set alarm manager
                            addAlarmSubject.onNext(reminder)

                            toastSubject.onNext(R.string.reminder_added_successfully)
                            backSubject.onNext(true)
                        },
                        {   //error
                                error ->
                            toastSubject.onNext(R.string.error_saving_reminder)
                        }
                    )
                 }
            })

    }


    fun handleSaveButton2() {


       if (reminder.text.isBlank()) {
           toastSubject.onNext(R.string.text_empty)
            return
        }
       else if(reminder.createdAt.timeInMillis <= Calendar.getInstance().timeInMillis){
           toastSubject.onNext(R.string.old_date_error)
       return
   }

        disposable.add(isSameTimeOfAnotherAlarm()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {sameTimeAlarms ->

                 if (sameTimeAlarms.size>0){
                    //there is another reminders near this reminder time so don't allow operation
                    toastSubject.onNext(R.string.another_reminder_in_proximity)
                }else {
                     saveReminder().observeOn(AndroidSchedulers.mainThread())
                         .subscribe(
                             { reminderId ->
                                 reminder.id = reminderId
                                 //this reminder could be an update for existing reminder so we cancel any ongoing alarms
                                 cancelAlarmSubject.onNext(reminder)
                                 // set alarm manager
                                 addAlarmSubject.onNext(reminder)

                                 toastSubject.onNext(R.string.reminder_added_successfully)
                                 backSubject.onNext(true)
                             },
                             {   //error
                                     error ->
                                 toastSubject.onNext(R.string.error_saving_reminder)
                             }
                         )
                 }
            })

    }

}

