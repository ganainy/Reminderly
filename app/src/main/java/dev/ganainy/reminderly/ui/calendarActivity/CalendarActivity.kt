package dev.ganainy.reminderly.ui.calendarActivity

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.databinding.ActivityCalendarBinding
import dev.ganainy.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import dev.ganainy.reminderly.ui.category_reminders.CategoryFragment
import dev.ganainy.reminderly.ui.category_reminders.CategoryType
import dev.ganainy.reminderly.ui.mainActivity.ICommunication
import dev.ganainy.reminderly.ui.reminderFragment.ReminderFragment
import io.reactivex.disposables.CompositeDisposable
import java.util.*

const val CATEGORY_FRAGMENT = "categoryFragment"
const val REMINDER_FRAGMENT = "reminderFragment"
class CalendarActivity : AppCompatActivity() ,ICommunication{

    private val todayDecorator = TodayDecorator()
    private lateinit var binding: ActivityCalendarBinding
    private val disposable = CompositeDisposable()
    private lateinit var viewModel: CalendarViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar)

        initViewModel()

        viewModel.getDaysContainingReminders()


        binding.calendarView.setOnDateChangedListener { _, clickedCalendarDay, _: Boolean ->
            viewModel.onCalendarDayClicked(clickedCalendarDay)
        }




        //set background for today date in calendar
        binding.calendarView.addDecorators(
            todayDecorator
        )
        //don't mark clicked day in calendar
       // binding.calendarView.selectionMode=MaterialCalendarView.SELECTION_MODE_NONE

        /**handle error passed from viewmodel via errorSubject*/
        disposable.add(viewModel.errorSubject.subscribe {errorString ->
            MyUtils.showCustomToast(this, R.string.error_retreiving_reminder)
        })


        /**handle navigating passed from viewmodel via fragmentCalendarSubject*/
        disposable.add(viewModel.fragmentCalendarSubject.subscribe {
            when(it.first){
                CATEGORY_FRAGMENT->{
                    openCategoryFragment(it.second)
                }
                REMINDER_FRAGMENT->{
                    openReminderFragment(Reminder(createdAt = it.second))
                }
            }
        })

        /**get days that has reminders and add DOT to them on calendar*/
        disposable.add(viewModel.activeReminderListSubject.subscribe {calendarDays->
            binding.calendarView.addDecorator(
                DotDecorator(
                    Color.RED,
                    calendarDays
                )
            )
            binding.calendarView.invalidateDecorators()
        })


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

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}


