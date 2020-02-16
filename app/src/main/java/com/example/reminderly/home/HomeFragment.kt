package com.example.reminderly.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.reminderly.MainActivityViewModel
import com.example.reminderly.R
import com.example.reminderly.all.AllFragment
import com.example.reminderly.databinding.HomeFragmentBinding
import com.example.reminderly.favorites.FavoritesFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : Fragment() {

/**this fragment only works as host for all and favorites fragments*/

private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var binding:HomeFragmentBinding

    companion object {
        fun newInstance() = HomeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupViewPager(view)

        //observe live data emitted from main activity to navigate to calendar fragment on calendar
        //image click
        mainActivityViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        navigateToCalendarFragment()
        navigateToReminderFragment()

    }

    private fun navigateToReminderFragment() {
      binding.floatingActionButton.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_reminderFragment) }
    }

    private fun navigateToCalendarFragment() {
        mainActivityViewModel.navigateToCalendarFragment.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(R.id.action_homeFragment_to_calendarFragment)
                mainActivityViewModel.doneNavigateToCalendarFragment()
            }
        })
    }

    private fun setupViewPager(view: View) {
        demoCollectionAdapter = DemoCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter
        //Connect tab layout with viewpager and give names,icons for tabs
        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.all_reminders)
                1 -> getString(R.string.favorite_reminders)
                else -> throw Exception("unknown tab")
            }
            tab.icon = when (position) {
                0 -> ResourcesCompat.getDrawable(resources, R.drawable.note_ic, null)
                1 -> ResourcesCompat.getDrawable(resources, R.drawable.favorite_ic, null)
                else -> throw Exception("unknown tab")
            }



        }.attach()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.title=resources.getString(R.string.reminders)
    }

}


class DemoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> AllFragment.newInstance()
            1 -> FavoritesFragment.newInstance()
            else -> throw Exception("unknown fragment")
        }
    }
}

