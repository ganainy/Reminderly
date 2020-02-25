package com.example.reminderly.ui.favoritesFragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reminderly.R
import com.example.reminderly.Utils.EventBus.FavoriteReminderEvent
import com.example.reminderly.databinding.FavoritesFragmentBinding
import com.example.reminderly.ui.basefragment.BaseFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FavoritesFragment : BaseFragment() {

    private var recyclerIntialized = false

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


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onFavoriteReminderEvent(event: FavoriteReminderEvent) {

        Log.d("DebugTag", "onFavoriteReminderEvent: ${event.favoriteReminders.size}")
        /**if all favorite reminders are empty show empty layout,else show recycler*/
        if (event.favoriteReminders.isEmpty()) {

            binding.noRemindersGroup.visibility = View.VISIBLE
            binding.reminderReycler.visibility = View.GONE

        } else {

            binding.noRemindersGroup.visibility = View.GONE
            binding.reminderReycler.visibility = View.VISIBLE


            initRecycler()
            adapter.submitList(event.favoriteReminders)

        }
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
