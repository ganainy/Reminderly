package dev.ganainy.reminderly.ui.reminderListFragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.databinding.ReminderListFragmentBinding
import dev.ganainy.reminderly.ui.basefragment.BaseFragment
import dev.ganainy.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class ReminderListFragment : BaseFragment() {

    private lateinit var binding: ReminderListFragmentBinding
    private lateinit var viewModel: ReminderListFragmentViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory
    val disposable = CompositeDisposable()


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



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initAdapter()
        initRecycler()
        initViewModel()

        viewModel.getAllRemindersFormatted()

        disposable.add(
            viewModel.reminderListSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { reminderListFormatted ->
                    showReminders(reminderListFormatted)
                })

        disposable.add(
            viewModel.errorSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { errorString ->
                    MyUtils.showCustomToast(requireContext(), R.string.error_retreiving_reminder)
                })

        disposable.add(
            viewModel.emptyListSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { isListEmpty ->
                    if (isListEmpty) showEmptyUi()
                    else hideEmptyUi()
                })

    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao


        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ReminderListFragmentViewModel::class.java)
    }


    private fun initRecycler() {

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
                        2 -> 1
                        else -> 2
                    }
                }

            })

            binding.reminderReycler.layoutManager = gridLayoutManager
        } else {
            binding.reminderReycler.layoutManager = LinearLayoutManager(requireContext())
        }

    }


    private fun showReminders(formattedReminderList: MutableList<Reminder>) {
        //recycler already initialized just refresh position
        adapter.submitList(formattedReminderList)
        adapter.notifyDataSetChanged()
    }

    private fun hideEmptyUi() {
        binding.noRemindersGroup.visibility = View.GONE
        binding.reminderReycler.visibility = View.VISIBLE
    }

    private fun showEmptyUi() {
        binding.noRemindersGroup.visibility = View.VISIBLE
        binding.reminderReycler.visibility = View.GONE
    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}


