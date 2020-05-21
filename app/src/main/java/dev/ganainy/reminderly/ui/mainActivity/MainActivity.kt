package dev.ganainy.reminderly.ui.mainActivity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.example.footy.database.ReminderDatabase
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.*
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.databinding.ActivityMainBinding
import dev.ganainy.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import dev.ganainy.reminderly.ui.calendarActivity.CalendarActivity
import dev.ganainy.reminderly.ui.category_reminders.CategoryFragment
import dev.ganainy.reminderly.ui.category_reminders.CategoryType
import dev.ganainy.reminderly.ui.privacyPolicyFragment.PrivacyPolicyFragment
import dev.ganainy.reminderly.ui.reminderFragment.ReminderFragment
import dev.ganainy.reminderly.ui.search_fragment.SearchFragment
import dev.ganainy.reminderly.ui.settings_fragment.SettingsFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_content.*
import java.util.*

private lateinit var lastUpdateOfTodayReminder: MutableList<Reminder>

class MainActivity : AppCompatActivity(), ICommunication {

    private lateinit var badgeView: TextView
    private val disposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentViewPagerAdapter: FragmentViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        //if this is first time use for app show guide/highlight for features
        if (MyUtils.isAppFirstUse(this)) {
            setupAppForFirstTimeUse()
        }



        checkNightMode()

        setupToolbar()

        initViewModel()

        setupViewPager()

        setupTablayoutWithViewpager()

        setupDrawerContent()

        showCalendarButtonHint()


        //handle add fab click
        binding.appContent.findViewById<FloatingActionButton>(R.id.addReminderFab)
            .setOnClickListener {
                openReminderFragment()
            }

    }



    /**this will show hint guide to promote user to click the calendar button , this will only work
     *  the first time user opens menu only*/
    private fun showCalendarButtonHint() {
        if (MyUtils.getInt(this@MainActivity, SHOWN_DRAWER_GUIDE) == 0) {
            //this is the first time user opens drawer
            binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {

                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                }

                override fun onDrawerClosed(drawerView: View) {

                }

                override fun onDrawerOpened(drawerView: View) {
                    if (MyUtils.getInt(this@MainActivity, SHOWN_DRAWER_GUIDE) == 1) {
                        return
                    }
                    MyUtils.putInt(this@MainActivity, SHOWN_DRAWER_GUIDE, 1)

                    TapTargetView.showFor(this@MainActivity,
                        TapTarget.forView(
                            nav_view.getHeaderView(0)
                                .findViewById<ImageView>(R.id.calendarImageView),
                            getString(R.string.calendar_button),
                            getString(R.string.click_to_add_reminders_from_calendar)
                        )
                            .tintTarget(false)
                            .transparentTarget(false),
                        object : TapTargetView.Listener() {})

                }

            })
        }
    }



    private fun checkNightMode() {
        val isNightModeEnabled = MyUtils.getInt(this, NIGHT_MODE_ENABLED)
        if (isNightModeEnabled == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else if (isNightModeEnabled == 1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun setupAppForFirstTimeUse() {
        //set default dnd time
        MyUtils.putInt(applicationContext, DONT_DISTURB_START_HOURS, 6)
        MyUtils.putInt(applicationContext, DONT_DISTURB_START_MINUTES, 0)
        MyUtils.putInt(applicationContext, DONT_DISTURB_END_HOURS, 18)
        MyUtils.putInt(applicationContext, DONT_DISTURB_END_MINUTES, 0)
        //show features guide
        TapTargetView.showFor(this@MainActivity,
            TapTarget.forView(
                addReminderFab,
                getString(R.string.add_button),
                getString(R.string.click_to_add_reminders)
            ).tintTarget(false)
                .transparentTarget(false),
            object : TapTargetView.Listener() {})


        //set first time flag to false
        MyUtils.putInt(applicationContext, FIRST_TIME_USE, 1)
    }


    override fun onResume() {
        super.onResume()
        if (intent.hasExtra("newReminder")) {//user clicked add reminder from notification
            openReminderFragment()
        }

        //set current device locale to show time/dates in that locale
        MyUtils.setLocale(Locale.getDefault().language)

    }



    /**observe ad clicks and block user temporarily if he clicked more than 3*/
    private fun observeClickedAdCount(){
        val pref: SharedPreferences =
          applicationContext
                .getSharedPreferences("MyPref", 0)
        val rxPreferences = RxSharedPreferences.create(pref)
        val shouldAllowPersistentNotification: com.f2prateek.rx.preferences2.Preference<Int> =
            rxPreferences.getInteger(AD_CLICK_PER_SESSION, -1)

        disposable.add(shouldAllowPersistentNotification.asObservable().subscribe {
            if (it>=5){
               //reset click counter
               MyUtils.putInt(this, AD_CLICK_PER_SESSION,0)
               //temporarily ban user for clicking 5 ads in one session
               layoutInflater.inflate(R.layout.multiple_ad_click_ban_layout,binding.fragmentContainer,true)
               // hide temporarily ban layout after 10 sec
                val mCountDownTimer = object : CountDownTimer(15 * 1000L, 15000) {
                    override fun onTick(millisUntilFinished: Long) {

                    }
                    override fun onFinish() {
                        binding.fragmentContainer.removeAllViews()
                    }
                }.start()


           }
        })
    }





    private fun observeDoneReminders() {
        disposable.add(
            viewModel.getDoneReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ doneReminderList ->


                showMenuItem(doneReminderList.size, R.id.done, CategoryType.DONE)


            }, { error ->
                MyUtils.showCustomToast(this@MainActivity, R.string.error_retreiving_reminder)
            })
        )
    }

    /**pass reminder with fragment creation depending if null(if creating new reminder) or not-null
     * (if editing existing reminder)*/
    private fun openReminderFragment(reminder: Reminder? = null) {
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
            ProvideDatabaseViewModelFactory(
                application,
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
            when (menuItem.itemId) {
                R.id.settings -> {
                    openSettingsFragment()
                }
                R.id.about -> {
                    openAboutFragment()
                }
                R.id.rate_app -> {
                    showRateAppDialog()
                }
            }
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

    private fun showRateAppDialog() {
        MaterialDialog(this).show {
            message(R.string.take_moment_to_rate_app)
            icon(R.drawable.ic_star_yellow)
           title(R.string.rate_app)
            positiveButton(R.string.proceed_to_store,click={
                openAppOnGooglePlay()
            })
            negativeButton(R.string.cancel,click = {
                it.cancel()
            })
        }
    }

    private fun openAppOnGooglePlay() {

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun openAboutFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, PrivacyPolicyFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun openSettingsFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }


    /**shows menu item with badge count of reminders in that certain reminders' category*/
    private fun showMenuItem(
        remindersLength: Int,
        menu_item_id: Int,
        categoryType: CategoryType
    ) {
        val menuItem = nav_view.menu.findItem(menu_item_id)

        if (remindersLength != 0) {
            menuItem.isVisible = true
            badgeView = menuItem?.actionView?.findViewById(R.id.countTextView)!!
            badgeView.text = remindersLength.toString()
        } else {
            menuItem.isVisible = false
        }


        /**show separate fragment for that reminder category on menu item click*/
        menuItem.setOnMenuItemClickListener {
            val ft = supportFragmentManager.beginTransaction()
            ft.add(
                R.id.fragmentContainer,
                CategoryFragment.newInstance(categoryType, null),
                "categoryFragment"
            )
            ft.addToBackStack(null)
            ft.commit()
            true
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toobar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                /**this home menu item might be back arrow(in fragments) or menu icon to open drawer
                 * (in activity) so we determine functionality of it here*/
                if (supportFragmentManager.backStackEntryCount > 0) {
                    super.onBackPressed()
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            R.id.search -> {
                openSearchFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openSearchFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(
            R.id.fragmentContainer,
            SearchFragment(),
            "searchFragment"
        )
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onBackPressed() {
        /**if there is fragment added on activity close it before closing whole activity*/
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }


    override fun onStart() {
        super.onStart()


        /**get done/upcoming/overdue/today reminders from db and show menu item if there is reminders  */
        observeDoneReminders()

        observeUpcomingReminders()
        observeOverdueReminders()
        observeTodayReminders()

        observeClickedAdCount()


    }

    private fun observeOverdueReminders() {
        disposable.add(
            viewModel.getOverdueReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ overdueReminders ->

                showMenuItem(overdueReminders.size, R.id.overdue, CategoryType.OVERDUE)

            }, { error ->
                MyUtils.showCustomToast(this@MainActivity, R.string.error_retreiving_reminder)

            })
        )
    }

     fun observeTodayReminders() {
        disposable.add(
            viewModel.getTodayReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ todayReminders ->

                showMenuItem(todayReminders.size, R.id.today, CategoryType.TODAY)

                //show persistent notification to help user add reminder from outside of app
                /**check if user allowed showing persistent notification
                 * 0-> allowed (default)
                 * 1-> not allowed
                 */
                if (MyUtils.getInt(this, ALLOW_PERSISTENT_NOTIFICATION) == 0) {
                    MyUtils.sendPersistentNotification(applicationContext,todayReminders)
                }

            }, { error ->
                MyUtils.showCustomToast(this@MainActivity, R.string.error_retreiving_reminder)

            })
        )
    }

    private fun observeUpcomingReminders() {
        disposable.add(
            viewModel.getUpcomingReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ upcomingReminderList ->

                showMenuItem(
                    upcomingReminderList.size,
                    R.id.upcoming,
                    CategoryType.UPCOMING
                )


            }, { error ->
                MyUtils.showCustomToast(this@MainActivity, R.string.error_retreiving_reminder)

            })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
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


/**interface for communication between activity and child fragments*/
interface ICommunication {
    fun setDrawerEnabled(enabled: Boolean)
    fun showReminderFragment(reminder: Reminder)
}



