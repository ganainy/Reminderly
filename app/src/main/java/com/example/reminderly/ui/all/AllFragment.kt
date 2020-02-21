package com.example.reminderly.ui.all

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.footy.database.ReminderDatabase
import com.example.ourchat.ui.chat.ReminderAdapter
import com.example.ourchat.ui.chat.ReminderClickListener
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.AllFragmentBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AllFragment : Fragment() {

    private val disposable = CompositeDisposable()
    private lateinit var binding:AllFragmentBinding
    private val adapter by lazy { ReminderAdapter(requireContext(),object :ReminderClickListener{
        override fun onReminderClick(reminder: Reminder) {
            Log.d("DebugTag", "onReminderClick: "+reminder)
        }

    }) }

    companion object {
        fun newInstance() = AllFragment()
    }

    private lateinit var viewModel: AllViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.all_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AllViewModel::class.java)
        // TODO: Use the ViewModel
    }


    override fun onStart() {
        super.onStart()


        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao
        disposable.add(reminderDatabaseDao.getAllReminders().subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()).subscribe ({reminderList ->
            if (reminderList.isNotEmpty()){
                //hide layout that says there is no reminders
                binding.noRemindersGroup.visibility=View.INVISIBLE


                    initRecycler()
                    adapter.submitList(reminderList)

            }
        },{error->
            Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                .show()
        }))

    }

    private fun initRecycler() {
        binding.reminderReycler.setHasFixedSize(true)
        binding.reminderReycler.adapter=adapter

    }


    override fun onStop() {
        super.onStop()
        disposable.clear()
    }



}
