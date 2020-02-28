package com.example.reminderly.ui.reminderListFragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ReminderListFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class ReminderListFragment : BaseFragment() {

    private var recyclerInitialized = false
    private lateinit var binding: ReminderListFragmentBinding
    private lateinit var viewModel: ReminderListFragmentViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory
    private val disposable=CompositeDisposable()

    companion object {

        fun newInstance() = ReminderListFragment()

        val overdueReminders = mutableListOf<Reminder>()
        val todayReminders = mutableListOf<Reminder>()
        val upcomingReminders = mutableListOf<Reminder>()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.reminder_list_fragment, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao


         viewModelFactory=
             ProvideDatabaseViewModelFactory(
                 requireActivity().application,
                 reminderDatabaseDao
             )
        viewModel = ViewModelProvider(this, viewModelFactory).get(ReminderListFragmentViewModel::class.java)


    }
    /**
     *show reminders in recycler*/
    private fun showReminders(
        overdueReminders: MutableList<Reminder>,
        todayReminders: MutableList<Reminder>,
        upcomingReminders: MutableList<Reminder>
    ) {
        if (overdueReminders.isEmpty() && todayReminders.isEmpty() && upcomingReminders.isEmpty()) {

            binding.noRemindersGroup.visibility = View.VISIBLE
            binding.reminderReycler.visibility = View.GONE

        } else {

            binding.noRemindersGroup.visibility = View.GONE
            binding.reminderReycler.visibility = View.VISIBLE

            val reminderListWithHeaders = mutableListOf<Reminder>()

            if (overdueReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 1))//add empty reminder with header value that will be used as header in recycler
                for (reminder in overdueReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }
            if (todayReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 2))//add empty reminder with header value that will be used as header in recycler
                for (reminder in todayReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }
            if (upcomingReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 3))//add empty reminder with header value that will be used as header in recycler
                for (reminder in upcomingReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }

            initRecycler()
            adapter.submitList(reminderListWithHeaders)

        }
    }

    private fun observeReminders() {
        disposable.add(
            viewModel.getAllReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ reminderList ->


             overdueReminders.clear()
             todayReminders.clear()
             upcomingReminders.clear()


                for (reminder in reminderList) {
                    val currentCalendar = Calendar.getInstance()
                    when {
                        DateUtils.isToday(reminder.createdAt.timeInMillis) -> {
                            todayReminders.add(reminder)
                        }
                        reminder.createdAt.before(currentCalendar) -> {
                            overdueReminders.add(reminder)
                        }
                        else -> {
                            upcomingReminders.add(reminder)
                        }
                    }
                }


          showReminders(
                    overdueReminders,
                    todayReminders,
                    upcomingReminders
                )

            }, { error ->
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.error_retreiving_reminder),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
        )
    }


    private fun initRecycler() {
        if (recyclerInitialized) return

        recyclerInitialized = true

        initAdapter()

        binding.reminderReycler.setHasFixedSize(true)
        binding.reminderReycler.adapter = adapter
        //Change layout manager depending on orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(requireActivity(), 2)
            //change span size of headers so header shows in row
            gridLayoutManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter.getItemViewType(position)) {
                        0 -> 1
                        else -> 2
                    }
                }

            })

            binding.reminderReycler.layoutManager = gridLayoutManager
        } else {
            binding.reminderReycler.layoutManager = LinearLayoutManager(requireContext())
        }


    }


    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onStart() {
        super.onStart()
        /**get all active reminders(not done) from db and show menu item for each type of active reminders
         * (overdue-today-upcoming) */
        observeReminders()
    }

}
