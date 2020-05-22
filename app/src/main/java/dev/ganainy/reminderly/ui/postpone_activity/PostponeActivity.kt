package dev.ganainy.reminderly.ui.postpone_activity

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.Utils.MyUtils.Companion.getReminderFromString
import dev.ganainy.reminderly.Utils.REMINDER
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_postpone.*

/**this activity has special theme in manifest so it is transparent && and special attributes
 *  (special affinity and launch mode) so it won't show in recent apps after i use it*/
class PostponeActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private lateinit var viewModel: PostponeViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory
    private lateinit var reminder :Reminder
    private  var minPicked:Int=0
    private  var hourPicked:Int=0
    private  var dayPicked:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postpone)

        initViewModel()

        setupNumberPickers()

        //get id of the reminder which will be postponed (passed from notification pending intent
        // coming from alarmService)
         val reminderString = intent.getStringExtra(REMINDER)
        reminder=reminderString?.getReminderFromString()?: return


        //delay reminder by the selected amount by user
        MyUtils.closeReminder(reminder,this)

        cancelButton.setOnClickListener {
            finish()
        }



        postponeButton.setOnClickListener {

            val postponedReminder = MyUtils.postponeReminder(
                reminder,
                applicationContext,
                dayPicked,
                hourPicked,
                minPicked
            )

            if (postponedReminder == null) {
                //postpone failed show error
                error_text.visibility = View.VISIBLE
            } else {
                error_text.visibility = View.GONE
                //update reminder with new postponed date
                disposable.add(viewModel.updateReminder(postponedReminder).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                        // set alarm
                        MyUtils.cancelAlarmManager(reminder,this@PostponeActivity )
                        MyUtils.addAlarmManager(
                            reminder,
                            this@PostponeActivity
                        )

                        MyUtils.showCustomToast(
                            this@PostponeActivity,
                            R.string.reminder_postponed
                        )

                        finish()
                    })
            }
        }
    }




    private fun setupNumberPickers() {
        val dayPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.dayPicker)
        dayPicker.maxValue = 30
        dayPicker.minValue = 0
        dayPicker.setOnValueChangedListener { _, _, newVal ->
          dayPicked=newVal
        }

        val hourPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.hourPicker)
        hourPicker.maxValue = 23
        hourPicker.minValue = 0
        hourPicker.setOnValueChangedListener { _, _, newVal ->
            hourPicked=newVal
        }


        val minutePicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.minutePicker)
        minutePicker.maxValue = 59
        minutePicker.minValue = 0
        minutePicker.setOnValueChangedListener { _, _, newVal ->
            minPicked=newVal
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


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}


