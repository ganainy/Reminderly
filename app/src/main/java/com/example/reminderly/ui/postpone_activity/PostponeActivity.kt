package com.example.reminderly.ui.postpone_activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
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

        val (dayPicker, hourPicker, minutePicker) = setupNumberPickers()

        //get id of the reminder which will be postponed (passed from notification pending intent)
        val reminderId = intent.getLongExtra("reminderId", -1L)


        disposable.add(
            viewModel.getReminderById(reminderId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { reminder ->
                    postponeButton.setOnClickListener {
                        val postponedReminder = MyUtils.postponeReminder(
                            reminder,
                            applicationContext,
                            dayPicker.value,
                            hourPicker.value,
                            minutePicker.value
                        )

                        if (postponedReminder == null) {
                            //postpone failed show error
                            error_text.visibility= View.VISIBLE
                        } else {
                            error_text.visibility= View.GONE
                            //update reminder with new postponed date
                            disposable.add(viewModel.updateReminder(postponedReminder)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    // set alarm
                                    MyUtils.addAlarm(
                                        reminder.id,
                                        applicationContext,
                                        reminder.createdAt.timeInMillis,
                                        reminder.repeat
                                    )

                                    Toast.makeText(
                                            this,
                                            getString(R.string.reminder_postponed),
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    finish()
                                })
                        }
                    }

                })

        cancelButton.setOnClickListener {
            finish()
        }

    }

    private fun setupNumberPickers(): Triple<NumberPicker, NumberPicker, NumberPicker> {
        val dayPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.dayPicker)
        dayPicker.maxValue = 30
        dayPicker.minValue = 0
        val hourPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.hourPicker)
        hourPicker.maxValue = 23
        hourPicker.minValue = 0
        val minutePicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.minutePicker)
        minutePicker.maxValue = 59
        minutePicker.minValue = 0
        return Triple(dayPicker, hourPicker, minutePicker)
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


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}


