package dev.ganainy.reminderly.ui.favoritesFragment

import android.annotation.SuppressLint
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
import dev.ganainy.reminderly.utils.MyUtils
import dev.ganainy.reminderly.databinding.FavoritesFragmentBinding
import dev.ganainy.reminderly.ui.baseFragment.BaseFragment
import dev.ganainy.reminderly.ui.baseFragment.ProvideDatabaseViewModelFactory
import io.reactivex.disposables.CompositeDisposable


class FavoritesFragment : BaseFragment() {

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

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initAdapter()
        initRecycler()
        initViewModel()

        disposable.add(viewModel.reminderListSubject.subscribe{favoriteReminderList->
            adapter.submitList(favoriteReminderList)
            adapter.notifyItemRangeChanged(0,favoriteReminderList.size)
        })

        disposable.add(viewModel.errorSubject.subscribe {
            MyUtils.showCustomToast(requireContext(), R.string.error_retreiving_favorite_reminder)
        })

        disposable.add(viewModel.emptyListSubject.subscribe {isEmptyList ->
            if (isEmptyList){
                showEmptyUi()
            }else{
                hideEmptyUi()
            }
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
            ViewModelProvider(this, viewModelFactory).get(FavoriteFragmentViewModel::class.java)
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

    private fun showEmptyUi(){
        binding.noRemindersGroup.visibility = View.VISIBLE
        binding.reminderReycler.visibility = View.GONE
    }

    private fun hideEmptyUi(){
        binding.noRemindersGroup.visibility = View.GONE
        binding.reminderReycler.visibility = View.VISIBLE
    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

}
