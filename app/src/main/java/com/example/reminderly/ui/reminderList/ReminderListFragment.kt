package com.example.reminderly.ui.reminderList

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.reminderly.Utils.EventBus.ReminderEvent
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.AllFragmentBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ReminderListFragment : Fragment() {

    private val disposable = CompositeDisposable()
    private var recyclerIntialized = false
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


    }


    /**
     * Once main activity get any reminder update this event will trigger and it will show reminders in recycler*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReminderEvent( event: ReminderEvent) {
      if (event.overdueReminders.isEmpty() && event.todayReminders.isEmpty() &&event.upcomingReminders.isEmpty() ){
          binding.noRemindersGroup.visibility = View.VISIBLE
      }else{
          val reminderListWithHeaders= mutableListOf<Reminder>()
          if (event.overdueReminders.isNotEmpty()) {
              reminderListWithHeaders.add(Reminder(header = 1))//add empty reminder with header value that will be used as header in recycler
              for (reminder in event.overdueReminders){
              reminderListWithHeaders.add(reminder)}
          }
          if (event.todayReminders.isNotEmpty()) {
              reminderListWithHeaders.add(Reminder(header = 2))//add empty reminder with header value that will be used as header in recycler
              for (reminder in event.todayReminders){
                  reminderListWithHeaders.add(reminder)}
          }
          if (event.upcomingReminders.isNotEmpty()) {
              reminderListWithHeaders.add(Reminder(header = 3))//add empty reminder with header value that will be used as header in recycler
              for (reminder in event.upcomingReminders){
                  reminderListWithHeaders.add(reminder)}
          }
          initRecycler()
          adapter.submitList(reminderListWithHeaders)
      }
    }




    private fun initRecycler() {
        if (recyclerIntialized) return

        recyclerIntialized=true
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
        EventBus.getDefault().unregister(this)

    }



    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }



}
