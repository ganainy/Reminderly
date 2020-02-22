package com.example.reminderly.ui.all

import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.example.footy.database.ReminderDatabase
import com.example.ourchat.ui.chat.ReminderAdapter
import com.example.ourchat.ui.chat.ReminderClickListener
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.AllFragmentBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ReminderListFragment : Fragment() {

    private val disposable = CompositeDisposable()
    private val recyclerIntialized = false
    private lateinit var binding: AllFragmentBinding
    private val adapter by lazy {
        ReminderAdapter(requireContext(), object : ReminderClickListener {
            override fun onReminderClick(reminder: Reminder) {
                //todo open reminder
            }

            override fun onFavoriteClick(reminder: Reminder) {
                disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { /*task completed*/ })
            }

            override fun onMenuClick(reminder: Reminder) {

                showOptionsSheet(reminder)
            }

        })
    }

    private fun showOptionsSheet(reminder: Reminder) {
        val items = listOf(
            BasicGridItem(R.drawable.ic_check_grey, getString(R.string.done)),
            BasicGridItem(R.drawable.ic_access_time_grey, getString(R.string.postpone)),
            BasicGridItem(R.drawable.ic_edit_grey, getString(R.string.edit)),
            BasicGridItem(R.drawable.ic_content_copy_grey, getString(R.string.copy)),
            BasicGridItem(R.drawable.ic_share_grey, getString(R.string.share)),
            BasicGridItem(R.drawable.ic_delete_grey, getString(R.string.delete))
        )

        MaterialDialog(requireContext(), BottomSheet()).show {
            gridItems(items) { _, index, item ->
               //todo handle sheet item clicks
            }
        }
    }

    companion object {
        fun newInstance() = ReminderListFragment()
    }

    private lateinit var viewModel: ReminderListViewModel
    private lateinit var viewModelFactory: ReminderListViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.all_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao

        viewModelFactory =
            ReminderListViewModelFactory(requireActivity().application, reminderDatabaseDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ReminderListViewModel::class.java)


        getAllReminders()


    }

    private fun getAllReminders() {
        disposable.add(
            viewModel.getAllReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ reminderList ->
                if (reminderList.isNotEmpty()) {

                    //add header for the first reminder if item in that category(overdue,today,upcoming)exists
                    val reminderListWithHeaders = addHeaders(reminderList)

                    initRecycler()
                    adapter.submitList(reminderListWithHeaders)

                } else {
                    binding.noRemindersGroup.visibility = View.VISIBLE
                }
            }, { error ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
        )
    }



    private fun addHeaders(reminderList: MutableList<Reminder>): MutableList<Reminder> {
        val reminderListWithHeaders = mutableListOf<Reminder>()

        //todo replace this with 3 lists coming from main
        reminderList.sortBy { it.createdAt.timeInMillis }

        var overdueHeader = false
        var todayHeader = false
        var upcomingHeader = false

        for (reminder in reminderList) {
            if (!overdueHeader && (reminder.createdAt.timeInMillis < Calendar.getInstance().timeInMillis)) {
                overdueHeader = true
                reminderListWithHeaders.add(Reminder(header = 1))//add empty reminder with header value that will be used as header in recycler
            } else if (!todayHeader && DateUtils.isToday(reminder.createdAt.timeInMillis)) {
                todayHeader = true
                reminderListWithHeaders.add(Reminder(header = 2))//add empty reminder with header value that will be used as header in recycler
            } else if (!upcomingHeader && (reminder.createdAt.timeInMillis > Calendar.getInstance().timeInMillis)) {
                upcomingHeader = true
                reminderListWithHeaders.add(Reminder(header = 3))//add empty reminder with header value that will be used as header in recycler
            }
            reminderListWithHeaders.add(reminder)
        }
        return reminderListWithHeaders
    }

    private fun initRecycler() {
        if (recyclerIntialized) return

        binding.reminderReycler.setHasFixedSize(true)
        binding.reminderReycler.adapter = adapter
        //Change layout manager depending on orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
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


}
