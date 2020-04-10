package com.example.reminderly.ui.mainActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.ALLOW_PERSISTENT_NOTIFICATION
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.Utils.NIGHT_MODE_ENABLED
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ActivityMainBinding
import com.example.reminderly.ui.aboutFragment.AboutFragment
import com.example.reminderly.ui.basefragment.ProvideDatabaseViewModelFactory
import com.example.reminderly.ui.calendarActivity.CalendarActivity
import com.example.reminderly.ui.category_reminders.CategoryFragment
import com.example.reminderly.ui.category_reminders.CategoryType
import com.example.reminderly.ui.reminderFragment.ReminderFragment
import com.example.reminderly.ui.search_fragment.SearchFragment
import com.example.reminderly.ui.settings_fragment.SettingsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_content.*
import java.util.*


private const val PERSISTENT_CHANNEL_ID = "primary_notification_channel"
private const val PERSISTENT_NOTIFICATION_ID = 0
private lateinit var lastUpdateOfTodayReminder:MutableList<Reminder>

class MainActivity : AppCompatActivity(), ICommunication {

    private lateinit var badgeView: TextView
    private val disposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentViewPagerAdapter: FragmentViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory
    private  val mNotifyManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        requestIgnoteBatteryOptimization()

        checkNightMode()

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

    }

    private fun requestIgnoteBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun checkNightMode() {
        val isNightModeEnabled = MyUtils.getInt(this, NIGHT_MODE_ENABLED)
        Log.d("DebugTag", "checkNightMode: ${isNightModeEnabled}")
        if (isNightModeEnabled == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else if (isNightModeEnabled == 1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }


    override fun onResume() {
        super.onResume()
        if ( intent.hasExtra("newReminder")){//user clicked add reminder from notification
            openReminderFragment()
        }

        
    }

    fun cancelPersistentNotification() {
        mNotifyManager.cancel(PERSISTENT_NOTIFICATION_ID)
    }


    /**show persistent notification to allow user to add reminder if app is closed*/
     fun sendPersistentNotification(todayReminders: MutableList<Reminder> = lastUpdateOfTodayReminder) {

        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {
            // Create a NotificationChannel
            val notificationChannel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "Reminder Persistent Notification", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Notification from Reminderly"
            mNotifyManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = getPersistentNotificationBuilder(todayReminders)
        mNotifyManager.notify(PERSISTENT_NOTIFICATION_ID, notificationBuilder?.build())

    }

    private fun getPersistentNotificationBuilder(todayReminders: MutableList<Reminder>): NotificationCompat.Builder? {
        val notificationButtonText= when (todayReminders.size) {
            0 ->getString(R.string.add_reminders)
            else->getString(R.string.add_other_reminders)
        }

        val notificationText = when (todayReminders.size) {
            0 ->getString(R.string.no_reminders_today)
            else->{
                var pastReminderOfToday=0
                var upcomingReminderOfToday=0
                for (reminder in todayReminders){
                    if (reminder.createdAt.before(Calendar.getInstance())){
                        pastReminderOfToday++
                    }else{
                        upcomingReminderOfToday++
                    }
                }
                getString(R.string.reminders_today,todayReminders.size,pastReminderOfToday,upcomingReminderOfToday)
            }
        }

        /**new reminder pending intent to pass to notification builder action*/
        val newReminderIntent = Intent(this, MainActivity::class.java)
        newReminderIntent.putExtra("newReminder","")
        newReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val newReminderPendingIntent = PendingIntent.getActivity(
            this,
            0, newReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        /**open app pending intent to pass to notification builder contentIntent*/
        val contentIntent = Intent(this, MainActivity::class.java)
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, PERSISTENT_CHANNEL_ID)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_bell_white)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setWhen(0)
            .addAction(
                R.drawable.ic_add_white,
                notificationButtonText,
                newReminderPendingIntent
            )
    }

 
 





    private fun observeDoneReminders() {
        disposable.add(
            viewModel.getDoneReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ doneReminderList ->


                showMenuItem(doneReminderList.size, R.id.done, CategoryType.DONE)


            }, { error ->
                MyUtils.showCustomToast(this@MainActivity,R.string.error_retreiving_reminder)
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
            //todo navigate
            when(menuItem.itemId){
                R.id.settings -> {
                    openSettingsFragment()
                }
                R.id.about -> {
                    openAboutFragment()
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

    private fun openAboutFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, AboutFragment())
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

    }

    private fun observeOverdueReminders() {
        disposable.add(
            viewModel.getOverdueReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ overdueReminders ->

                showMenuItem(overdueReminders.size, R.id.overdue, CategoryType.OVERDUE)

            }, { error ->
                MyUtils.showCustomToast(this@MainActivity,R.string.error_retreiving_reminder)

            })
        )
    }

    private fun observeTodayReminders() {
        disposable.add(
            viewModel.getTodayReminders().subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ todayReminders ->

                lastUpdateOfTodayReminder=todayReminders

                showMenuItem(todayReminders.size, R.id.today, CategoryType.TODAY)

                //show persistent notification to help user add reminder from outside of app
                /**check if user allowed showing persistent notification
                 * 0-> allowed (default)
                 * 1-> not allowed
                */
                if (MyUtils.getInt(this,ALLOW_PERSISTENT_NOTIFICATION)==0)
                {
                    sendPersistentNotification(todayReminders)
                }

            }, { error ->
                MyUtils.showCustomToast(this@MainActivity,R.string.error_retreiving_reminder)

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
                MyUtils.showCustomToast(this@MainActivity,R.string.error_retreiving_reminder)

            })
        )
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


/**interface for communication between activity and child fragments*/
interface ICommunication {
    fun setDrawerEnabled(enabled: Boolean)
    fun showReminderFragment(reminder: Reminder)
}



