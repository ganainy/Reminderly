package com.example.reminderly.ui.favoritesFragment

import android.content.res.Configuration
import android.os.Bundle
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
import com.example.reminderly.databinding.FavoritesFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FavoritesFragment : BaseFragment() {

    private var recyclerIntialized = false
    private val disposable = CompositeDisposable()
    private lateinit var viewModel: FavoriteFragmentViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    private lateinit var binding: FavoritesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.favorites_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao

        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(FavoriteFragmentViewModel::class.java)

        /**get favorite reminders from db and pass them to favorites fragment  */
        observeFavoriteReminders()

    }



    private fun observeFavoriteReminders() {
        disposable.add(
            viewModel.getFavoriteReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ favoriteReminderList ->

                /**if all favorite reminders are empty show empty layout,else show recycler*/
                if (favoriteReminderList.isEmpty()) {

                    binding.noRemindersGroup.visibility = View.VISIBLE
                    binding.reminderReycler.visibility = View.GONE

                } else {

                    binding.noRemindersGroup.visibility = View.GONE
                    binding.reminderReycler.visibility = View.VISIBLE


                    initRecycler()
                    adapter.submitList(favoriteReminderList)

                }


            }, { error ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_retreiving_favorite_reminder),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
        )
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
