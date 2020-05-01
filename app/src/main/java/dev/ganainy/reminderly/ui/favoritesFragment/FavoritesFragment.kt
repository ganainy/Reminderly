package dev.ganainy.reminderly.ui.favoritesFragment

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
import dev.ganainy.reminderly.databinding.FavoritesFragmentBinding
import dev.ganainy.reminderly.ui.basefragment.BaseFragment
import dev.ganainy.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
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
                    binding.reminderReycler.visibility = View.INVISIBLE

                } else {

                    binding.noRemindersGroup.visibility = View.INVISIBLE
                    binding.reminderReycler.visibility = View.VISIBLE


                    initRecycler()
                    adapter.submitList(favoriteReminderList)

                }


            }, { error ->
                MyUtils.showCustomToast(requireContext(),R.string.error_retreiving_favorite_reminder)

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
