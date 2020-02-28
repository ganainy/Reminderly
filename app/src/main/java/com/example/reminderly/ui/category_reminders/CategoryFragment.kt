package com.example.reminderly.ui.category_reminders

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.databinding.CategoryFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import com.example.reminderly.ui.mainActivity.ICommunication
import com.example.reminderly.ui.mainActivity.MainActivityViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class CategoryFragment : BaseFragment() {

    private val disposable = CompositeDisposable()
    private lateinit var binding: CategoryFragmentBinding
    private var recyclerInitialized = false
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    companion object {
        fun newInstance(
            categoryType: CategoryType,
            calendar: Calendar?
        ): CategoryFragment {
            val fragment = CategoryFragment()
            fragment.arguments = bundleOf("categoryType" to categoryType,
                "calendar" to calendar)
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding= DataBindingUtil.inflate(inflater,R.layout.category_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        /**reminders passed from activity*/
        val categoryType = arguments?.getSerializable("categoryType")  as CategoryType

        setupToolbar(categoryType)

        initViewModel()

        /**get reminders on single date(if caller is calendar activity) or get reminders of certain
         * category(if caller is main activity)*/
        if (categoryType==CategoryType.DATE) {
            getRemindersWithDate(arguments?.getSerializable("calendar")  as Calendar)
        } else {
            getReminders(categoryType)
        }

    }

    /**Get all calendars from the start of the passed calendar day to end of it*/
    private fun getRemindersWithDate(dateStart: Calendar) {


        val dateEnd=Calendar.getInstance().apply {
            set(Calendar.YEAR,dateStart.get(Calendar.YEAR))
            set(Calendar.MONTH,dateStart.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH,dateStart.get(Calendar.DAY_OF_MONTH)+1)
            set(Calendar.HOUR_OF_DAY,dateStart.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE,dateStart.get(Calendar.MINUTE))
            set(Calendar.SECOND,dateStart.get(Calendar.SECOND))
        }



        disposable.add(
            viewModel.getRemindersAtDate(dateStart,dateEnd).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ dateReminders ->

                if (dateReminders.isEmpty()) {

                    binding.noRemindersGroup.visibility = View.VISIBLE
                    binding.reminderReycler.visibility = View.GONE

                } else {

                    /**show title with reminders date*/
                    binding.toolbar.title=resources.getString(R.string.date_reminders,(
                            MyUtils.formatDate(dateReminders[0].createdAt.time)))

                    binding.noRemindersGroup.visibility = View.GONE
                    binding.reminderReycler.visibility = View.VISIBLE


                    initRecycler()
                    adapter.submitList(dateReminders)
                }

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

    private fun setupToolbar(categoryType: CategoryType) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.title= when(categoryType){
            CategoryType.TODAY ->{getString(R.string.today_reminders)}
            CategoryType.OVERDUE ->{getString(R.string.overdue_reminders)}
            CategoryType.UPCOMING ->{getString(R.string.upcoming_reminders)}
            CategoryType.DONE ->{getString(R.string.done_reminders)}
            CategoryType.DATE ->{""}
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun initViewModel() {
        val reminderDatabaseDao =
            ReminderDatabase.getInstance(requireActivity()).reminderDatabaseDao
        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )
        viewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            ).get(MainActivityViewModel::class.java)
    }


    /**get all reminder in certain category(done/upcoming/today/overdue)*/
    private fun getReminders(categoryType: CategoryType) {


        disposable.add(
            viewModel.getCategoryReminders(categoryType).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ categoryReminders ->


                if (categoryReminders.isEmpty()) {

                    binding.noRemindersGroup.visibility = View.VISIBLE
                    binding.reminderReycler.visibility = View.GONE

                } else {


                    binding.noRemindersGroup.visibility = View.GONE
                    binding.reminderReycler.visibility = View.VISIBLE


                    initRecycler()
                    adapter.submitList(categoryReminders)
                }


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


    override fun onStart() {
        super.onStart()
            /**this will be invoked to lock drawer only if parent of this fragment is main*/
            (requireActivity() as? ICommunication)?.setDrawerEnabled(false)

    }

    override fun onStop() {
        super.onStop()
        /**this will be invoked to un-lock drawer only if parent of this fragment is main*/
        (requireActivity() as? ICommunication)?.setDrawerEnabled(true)
    }

}

