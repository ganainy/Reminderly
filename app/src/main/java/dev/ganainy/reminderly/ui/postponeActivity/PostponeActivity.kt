package dev.ganainy.reminderly.ui.postponeActivity

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.ui.baseFragment.ProvideDatabaseViewModelFactory
import dev.ganainy.reminderly.utils.MyUtils
import dev.ganainy.reminderly.utils.MyUtils.Companion.getReminderFromString
import dev.ganainy.reminderly.utils.REMINDER
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_postpone.*

/**this activity has special theme in manifest so it is transparent && and special attributes
 *  (special affinity and launch mode) so it won't show in recent apps after i use it*/
class PostponeActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private lateinit var viewModel: PostponeViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory
    private lateinit var reminder: Reminder
    private var minPicked: Int = 0
    private var hourPicked: Int = 0
    private var dayPicked: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postpone)

        initViewModel()
        setupNumberPickers()

        //get id of the reminder which will be postponed (passed from notification pending intent
        // coming from alarmService)
        val reminderString = intent.getStringExtra(REMINDER)
        reminder = reminderString?.getReminderFromString() ?: return


        //cancel notification UI
        viewModel.stopReminderUi(reminder)

        cancelButton.setOnClickListener {
            finish()
        }

        postponeButton.setOnClickListener {
            viewModel.postponeReminder(
                reminder,
                dayPicked,
                hourPicked,
                minPicked
            )
        }


        disposable.add(viewModel.errorSubject.subscribe {hasError ->
            if (hasError)showErrorUi()
            else hideErrorUi()
        })

        disposable.add(viewModel.errorSubject.subscribe {shouldNavigateBack ->
            finish()
        })


        disposable.add(viewModel.toastSubject.subscribe {stringResourceId ->
            MyUtils.showCustomToast(
                this,
                stringResourceId
            )
        })



    }

    private fun hideErrorUi() {
        error_text.visibility = View.GONE
    }

    private fun showErrorUi() {
        error_text.visibility = View.VISIBLE
    }


    private fun setupNumberPickers() {
        val dayPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.dayPicker)
        dayPicker.maxValue = 30
        dayPicker.minValue = 0
        dayPicker.setOnValueChangedListener { _, _, newVal ->
            dayPicked = newVal
        }

        val hourPicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.hourPicker)
        hourPicker.maxValue = 23
        hourPicker.minValue = 0
        hourPicker.setOnValueChangedListener { _, _, newVal ->
            hourPicked = newVal
        }


        val minutePicker = custom_postpone_dialog.findViewById<NumberPicker>(R.id.minutePicker)
        minutePicker.maxValue = 59
        minutePicker.minValue = 0
        minutePicker.setOnValueChangedListener { _, _, newVal ->
            minPicked = newVal
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


