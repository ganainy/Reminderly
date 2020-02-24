package com.example.reminderly.ui.mainActivity

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.EventBus.ReminderEvent
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ActivityMainBinding
import com.example.reminderly.ui.calendarActivity.CalendarActivity
import com.example.reminderly.ui.reminderFragment.ReminderFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_content.*
import org.greenrobot.eventbus.EventBus
import java.util.*


class MainActivity : AppCompatActivity(), ICommunication {

    private lateinit var badgeView: TextView
    private val disposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentViewPagerAdapter: FragmentViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: MainActivityViewModelFactory


    companion object {
        val overdueReminders = mutableListOf<Reminder>()
        val todayReminders = mutableListOf<Reminder>()
        val upcomingReminders = mutableListOf<Reminder>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        setupToolbar()

        initViewModel()

        setupViewPager()

        setupTablayoutWithViewpager()

        setupDrawerContent()


        //handle add fab click
        binding.appContent.findViewById<FloatingActionButton>(R.id.addReminderFab)
            .setOnClickListener {

                openReminderFragment()
            }


        /**get all active reminders(not done) from db and show menu item for each type of active reminders
         * (overdue-today-upcoming) */
        disposable.add(
            viewModel.getAllReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ reminderList ->



                overdueReminders.clear()
                todayReminders.clear()
                upcomingReminders.clear()


                for (reminder in reminderList) {
                    when {
                        DateUtils.isToday(reminder.createdAt.timeInMillis) -> {
                            todayReminders.add(reminder)
                        }
                        reminder.createdAt.timeInMillis < Calendar.getInstance().timeInMillis -> {
                            overdueReminders.add(reminder)
                        }
                        reminder.createdAt.timeInMillis > Calendar.getInstance().timeInMillis -> {
                            upcomingReminders.add(reminder)
                        }
                    }
                }


                /**if there is overdue/today/upcoming reminders add them as tab in drawer menu
                 * with their count or hide menu tab if no reminders */
                showMenuItem(overdueReminders, R.id.overdue)
                showMenuItem(todayReminders, R.id.today)
                showMenuItem(upcomingReminders, R.id.upcoming)


                /**pass reminders to reminder list fragment*/
                EventBus.getDefault()
                    .post(ReminderEvent(overdueReminders, todayReminders, upcomingReminders))

            }, { error ->
                Toast.makeText(
                    this,
                    getString(R.string.error_retreiving_reminder),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
        )


        /**get done reminders from db and show menu item if there is done reminders  */
        disposable.add(
            viewModel.getDoneReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ doneReminderList ->


                if (doneReminderList.isNotEmpty()) {
                    showMenuItem(doneReminderList, R.id.done)
                }


            }, { error ->
                Toast.makeText(
                    this,
                    getString(R.string.error_retreiving_reminder),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
        )


    }

    private fun openReminderFragment(reminder: Reminder?=null) {
        /**pass reminder with fragment creation depending if null or not*/
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        if (reminder != null)
            ft.add(
                R.id.fragmentContainer,
                ReminderFragment.newInstance(reminder),
                "reminderFragment"
            )
        else
            ft.add(R.id.fragmentContainer, ReminderFragment(), "reminderFragment")
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun setupToolbar() {
        val toolbar = binding.appContent.findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        //add menu icon to toolbar (don't forget to override on option item selected for android.R.id.home to open drawer)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(this).reminderDatabaseDao
        viewModelFactory =
            MainActivityViewModelFactory(
                reminderDatabaseDao
            )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
    }


    private fun setupViewPager() {

        fragmentViewPagerAdapter =
            FragmentViewPagerAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = fragmentViewPagerAdapter
    }

    private fun setupTablayoutWithViewpager() {
        //Connect tab layout with app_content and give names,icons for tabs
        TabLayoutMediator(tab_layout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.all_reminders)
                1 -> getString(R.string.favorite_reminders)
                else -> throw Exception("unknown tab")
            }
            tab.icon = when (position) {
                0 -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.note_ic_white, null
                )
                1 -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.favorite_ic, null
                )
                else -> throw Exception("unknown tab")
            }
        }.attach()
    }

    private fun setupDrawerContent() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            //todo navigate
            binding.drawerLayout.closeDrawers()
            true
        }


        //set date in navigation drawer header
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.dateTextView).text =
            MyUtils.getCurrentDateFormatted()

        //open calendar on calendar image click
        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.calendarImageView)
            .setOnClickListener {
                startActivity(Intent(this, CalendarActivity::class.java))
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }
    }


    private fun showMenuItem(
        reminders: MutableList<Reminder>,
        menu_item_id: Int
    ) {
        val menuItem = nav_view.menu.findItem(menu_item_id)

        if (reminders.isNotEmpty()) {
            menuItem.isVisible = true
            badgeView = menuItem?.actionView?.findViewById(R.id.countTextView)!!
            badgeView.text = reminders.size.toString()
        } else {
            menuItem.isVisible = false
        }




        menuItem.setOnMenuItemClickListener {
            //todo open fragmnent to show reminders
            true
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toobar_menu, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo use this to search through reminders
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }


    override fun onStop() {
        super.onStop()
        disposable.clear()
    }


    /**this method called from fragments to lock/unlock drawer*/
    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED
        else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawer_layout.setDrawerLockMode(lockMode)
    }

    /**this method called from fragment to open and edit certain reminder*/
    override fun showReminderFragment(reminder: Reminder) {
        openReminderFragment(reminder)
    }
}


interface ICommunication {
    fun setDrawerEnabled(enabled: Boolean)
    fun showReminderFragment(reminder: Reminder)
}



