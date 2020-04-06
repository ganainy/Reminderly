package com.example.reminderly.ui.calendarActivity

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ActivityCalendarBinding
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import com.example.reminderly.ui.category_reminders.CategoryFragment
import com.example.reminderly.ui.category_reminders.CategoryType
import com.example.reminderly.ui.mainActivity.ICommunication
import com.example.reminderly.ui.reminderFragment.ReminderFragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class CalendarActivity : AppCompatActivity() ,ICommunication{

    private val todayDecorator = TodayDecorator()

    private lateinit var calendarDays: MutableList<CalendarDay>
    private lateinit var binding: ActivityCalendarBinding

    private val disposable = CompositeDisposable()
    private lateinit var viewModel: CalendarViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_calendar
        )



        initViewModel()

        observeActiveReminders()


        //set background for today date in calendar
        binding.calendarView.addDecorators(
            todayDecorator
        )
        //don't mark clicked day in calendar
       // binding.calendarView.selectionMode=MaterialCalendarView.SELECTION_MODE_NONE

        //handle click of calendar view
        binding.calendarView.setOnDateChangedListener { _, clickedCalendarDay, _: Boolean ->
            if (::calendarDays.isInitialized) {
                for (calendarDay in calendarDays) {
                    if (clickedCalendarDay == calendarDay) {
                        /**if clicked calendar in calendarDays this means clicked date has reminders
                         * so open category fragment and show them*/
                        openCategoryFragment(MyUtils.calendarDayToCalendar(clickedCalendarDay))
                        return@setOnDateChangedListener
                    }
                }
                /**if clicked calendar not in calendarDays this means clicked date has no reminders
                 * so open new reminder fragment with this date*/
                openReminderFragment(Reminder(
                    createdAt = MyUtils.calendarDayToCalendar(
                        clickedCalendarDay
                    )
                ))

            }
        }

    }

    /**pass the calendar to category fragment to get all reminders in that day*/
    private fun openCategoryFragment(clickedCalendar: Calendar) {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(
            R.id.fragmentContainer,
            CategoryFragment.newInstance(
                CategoryType.DATE,
                clickedCalendar
            ),
            "categoryFragment"
        )
        ft.addToBackStack(null)
        ft.commit()
    }



    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(this).reminderDatabaseDao
        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                application,
                reminderDatabaseDao
            )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(CalendarViewModel::class.java)
    }

    private fun observeActiveReminders() {
        disposable.add(
            viewModel.getActiveReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ activeReminderList ->

                calendarDays = MyUtils.getCalendarDays(activeReminderList)
                binding.calendarView.addDecorator(
                    DotDecorator(
                        Color.RED,
                        calendarDays
                    )
                )
                binding.calendarView.invalidateDecorators()

            }, { error ->
                MyUtils.showCustomToast(this@CalendarActivity,R.string.error_retreiving_reminder)

            })
        )
    }

    override fun setDrawerEnabled(enabled: Boolean) {
    }

    override fun showReminderFragment(reminder: Reminder) {
        openReminderFragment(reminder)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                    super.onBackPressed() }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openReminderFragment(reminder: Reminder) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
      ft.apply {
          add(R.id.fragmentContainer, ReminderFragment.newInstance(reminder), "reminderFragment")
          addToBackStack(null)
          commit()
      }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}


