package com.example.reminderly.ui.mainActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.reminderly.ui.favoritesFragment.FavoritesFragment
import com.example.reminderly.ui.reminderListFragment.ReminderListFragment

class FragmentViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> ReminderListFragment.newInstance()
            1 -> FavoritesFragment.newInstance()
            else -> throw Exception("unknown fragment")
        }
    }
}
