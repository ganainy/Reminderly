package com.example.reminderly.ui.search_fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.SearchFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SearchFragment : BaseFragment() {
    private lateinit var binding: SearchFragmentBinding
    private lateinit var viewModelFactory:ProvideDatabaseViewModelFactory
    private val disposable= CompositeDisposable()
    private var recyclerInitialized=false

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()

        binding.searchEditText.requestFocus()
        MyUtils.showKeyboard(requireContext())
        binding.searchEditText.afterTextChanged {
            observeReminders(it)
        }


        binding.backButton.setOnClickListener {
            MyUtils.hideKeyboard(requireContext(),binding.searchEditText)
            requireActivity().onBackPressed()
        }

    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao


        viewModelFactory=
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(SearchViewModel::class.java)
    }


    private fun observeReminders(query: String) {
        disposable.add(
            viewModel.getAllReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ reminderList ->

                val filteredReminderList= mutableListOf<Reminder>()
                for (reminder in reminderList){
                    if (reminder.text.contains(query)){
                        filteredReminderList.add(reminder)
                    }
                }

                if (filteredReminderList.isEmpty()||binding.searchEditText.text.isBlank()){

                    binding.noRemindersGroup.visibility = View.VISIBLE
                    binding.reminderReycler.visibility = View.GONE
                }else{
                    binding.noRemindersGroup.visibility = View.GONE
                    binding.reminderReycler.visibility = View.VISIBLE
                    initRecycler()
                    adapter.submitList(filteredReminderList)
                }




            }, { error ->
                MyUtils.showCustomToast(requireContext(),R.string.error_retreiving_reminder)

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

}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

