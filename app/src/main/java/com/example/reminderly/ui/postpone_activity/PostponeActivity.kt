package com.example.reminderly.ui.postpone_activity

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import com.example.reminderly.ui.mainActivity.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_postpone.*
import java.util.*

/**this activity has special theme in manifest so it is transparent && and special attributes
 *  (special affinity and launch mode) so it won't show in recent apps after i use it*/
class PostponeActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private lateinit var viewModel: PostponeViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postpone)

        initViewModel()

        //get id of the reminder which will be postponed (passed from notification pending intent)
        val reminderId = intent.getLongExtra("reminderId", -1L)

        //cancel notification
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(reminderId.toInt())

        disposable.add(
        viewModel.getReminderById(reminderId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe{reminder ->
                postponeButton.setOnClickListener {
                    when( radioGroup.checkedRadioButtonId){
                        R.id.radio_five -> postpone(reminder,minute=5)
                        R.id.radio_fiften -> postpone(reminder,minute=15)
                        R.id.radio_thirty -> postpone(reminder,minute=30)
                        R.id.radio_hour -> postpone(reminder,minute=60)
                    }
                }
            })

            cancelButton.setOnClickListener {
                finish()
            }

    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(this).reminderDatabaseDao
        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                application,
                reminderDatabaseDao
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(PostponeViewModel::class.java)
    }

    private fun postpone(reminder: Reminder, minute: Int, hour: Int = 0, day: Int = 0) {

        /** will check that the new reminder date is bigger than current date; because its useless
         *  to postpone reminder to a previous date*/

        reminder.createdAt.apply {
            add(Calendar.MINUTE, minute)
            add(Calendar.HOUR_OF_DAY, hour)
            add(Calendar.DAY_OF_MONTH, day)
        }


        if (reminder.createdAt.before(Calendar.getInstance())) {
            Toast.makeText(
                    this,
                    getString(R.string.must_be_upcoming_date),
                    Toast.LENGTH_LONG
                )
                .show()
            /*reminder should return to original value since update won't be saved*/
            reminder.createdAt.apply {
                add(Calendar.MINUTE, -minute)
                add(Calendar.HOUR_OF_DAY, -hour)
                add(Calendar.DAY_OF_MONTH, -day)
            }
            finish()

            return
        }


        //update reminder with new postponed date
        disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //cancel old alarm
                MyUtils.cancelAlarm(reminder.id,applicationContext)
                //get updated reminder date and set new alarm
                MyUtils.addAlarm(reminder.id,applicationContext,reminder.createdAt.timeInMillis)

                Toast.makeText(this, getString(R.string.reminder_postponed), Toast.LENGTH_SHORT).show()

                finish()

            })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}


