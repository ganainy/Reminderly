package dev.ganainy.reminderly.ui.categoryFragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.databinding.CategoryFragmentBinding
import dev.ganainy.reminderly.ui.baseFragment.BaseFragment
import dev.ganainy.reminderly.ui.mainActivity.ICommunication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.*

const val CATEGORY_TYPE="categoryType"
const val CALENDAR="calendar"
class CategoryFragment : BaseFragment() {

    private val disposable = CompositeDisposable()
    private lateinit var binding: CategoryFragmentBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var viewModelFactory: CategoryViewModelFactory

    companion object {
        fun newInstance(
            categoryType: CategoryType,
            calendar: Calendar
        ): CategoryFragment {
            val fragment = CategoryFragment()
            fragment.arguments = bundleOf(CATEGORY_TYPE to categoryType,
                CALENDAR to calendar)
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
        val categoryType = arguments?.getSerializable(CATEGORY_TYPE)  as CategoryType

        setupToolbar(categoryType)
        initAdapter()
        initRecycler()
        val dayCalendar = arguments?.getSerializable(CALENDAR) as Calendar
        initViewModel(dayCalendar)

        /**get reminders on single date(if caller is calendar activity) or get reminders of certain
         * category(if caller is main activity)*/
        if (categoryType==CategoryType.DATE) {
            viewModel.getSpecificDateReminders()
        } else {
            viewModel.getSpecificCategoryReminders(categoryType)
        }


        disposable.add(
            viewModel.reminderListSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { reminderListFormatted ->
                    showReminders(reminderListFormatted)
                })

        disposable.add(
            viewModel.toastSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { stringResourceId ->
                    MyUtils.showCustomToast(requireContext(),stringResourceId)
                })

        disposable.add(
            viewModel.emptyListSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { isListEmpty ->
                    if (isListEmpty) showEmptyUi()
                    else hideEmptyUi()
                })

        disposable.add(viewModel.toolbarSubject.subscribe {toolbarTitle->
            binding.toolbar.title=toolbarTitle
        })

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


    private fun initViewModel(dayCalendar: Calendar) {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao

        viewModelFactory =
            CategoryViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao,
                dayCalendar
            )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(CategoryViewModel::class.java)
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

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}

