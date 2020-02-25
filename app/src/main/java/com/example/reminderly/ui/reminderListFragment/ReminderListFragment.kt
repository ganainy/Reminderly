package com.example.reminderly.ui.reminderListFragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminderly.R
import com.example.reminderly.Utils.EventBus.ReminderEvent
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ReminderListFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ReminderListFragment : BaseFragment() {

    private var recyclerIntialized = false
    private lateinit var binding: ReminderListFragmentBinding

    companion object {
        fun newInstance() = ReminderListFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.reminder_list_fragment, container, false)
        return binding.root
    }


    /**
     * Once main activity get any reminder update this event will trigger and it will show reminders in recycler*/
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onReminderEvent(event: ReminderEvent) {

        /**if all reminders are empty show empty layout,else show recycler with header above each type of reminder*/
        if (event.overdueReminders.isEmpty() && event.todayReminders.isEmpty() && event.upcomingReminders.isEmpty()) {

            binding.noRemindersGroup.visibility = View.VISIBLE
            binding.reminderReycler.visibility = View.GONE

        } else {

            binding.noRemindersGroup.visibility = View.GONE
            binding.reminderReycler.visibility = View.VISIBLE

            val reminderListWithHeaders = mutableListOf<Reminder>()

            if (event.overdueReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 1))//add empty reminder with header value that will be used as header in recycler
                for (reminder in event.overdueReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }
            if (event.todayReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 2))//add empty reminder with header value that will be used as header in recycler
                for (reminder in event.todayReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }
            if (event.upcomingReminders.isNotEmpty()) {
                reminderListWithHeaders.add(Reminder(header = 3))//add empty reminder with header value that will be used as header in recycler
                for (reminder in event.upcomingReminders) {
                    reminderListWithHeaders.add(reminder)
                }
            }

            initRecycler()
            adapter.submitList(reminderListWithHeaders)

        }
    }



    private fun initRecycler() {
        if (recyclerIntialized) return

        recyclerIntialized = true

        initAdapter()

        binding.reminderReycler.setHasFixedSize(true)
        binding.reminderReycler.adapter = adapter
        //Change layout manager depending on orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
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




}
