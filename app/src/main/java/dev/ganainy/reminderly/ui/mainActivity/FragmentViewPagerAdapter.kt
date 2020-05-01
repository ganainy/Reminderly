package dev.ganainy.reminderly.ui.mainActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.ganainy.reminderly.ui.favoritesFragment.FavoritesFragment
import dev.ganainy.reminderly.ui.reminderListFragment.ReminderListFragment

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
