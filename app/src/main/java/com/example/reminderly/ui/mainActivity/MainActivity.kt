package com.example.reminderly.ui.mainActivity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.reminderly.R
import com.example.reminderly.Utils.DateUtils
import com.example.reminderly.databinding.ActivityMainBinding
import com.example.reminderly.ui.calendarActivity.CalendarActivity
import com.example.reminderly.ui.reminderActivity.ReminderActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_content.*


class MainActivity : AppCompatActivity() {


    private val fragmentTransaction by lazy { supportFragmentManager.beginTransaction() }
    private lateinit var binding: ActivityMainBinding
    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )


        val toolbar = binding.appContent.findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //add menu icon to toolbar (don't forget to override on option item selected for android.R.id.home to open drawer)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        setupViewPager()

        setupTablayoutWithViewpager()

        setupDrawerContent()


        //handle add fab click
        binding.appContent.findViewById<FloatingActionButton>(R.id.addReminderFab)
            .setOnClickListener {
                startActivity(Intent(this,ReminderActivity::class.java))
            }

        //set date in navigation drawer header
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.dateTextView).text =
            DateUtils.getCurrentDateFormatted()

        //open calendar on calendar image click
        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.calendarImageView)
            .setOnClickListener {
                startActivity(Intent(this,CalendarActivity::class.java))
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }

        //addItemsToMenu()
    }






    private fun setupViewPager() {

        demoCollectionAdapter =
            DemoCollectionAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter
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
                0 -> ResourcesCompat.getDrawable(resources,
                    R.drawable.note_ic, null)
                1 -> ResourcesCompat.getDrawable(resources,
                    R.drawable.favorite_ic, null)
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
    }

    private fun addItemsToMenu() {
        //todo use this to add item programmatically to navigation drawer
        val menu: Menu = nav_view.menu
        for (i in 1..3) {
            val menuItem = menu.add("Runtime item $i")
            menuItem.icon = ResourcesCompat.getDrawable(resources,
                R.drawable.note_ic, null)

            //create linear layout with textview in middle of it to allign it with menu item
            val linearLayout = LinearLayout(this)
            linearLayout.gravity = Gravity.CENTER

            val textView = TextView(this)
            textView.apply {
                setPadding(16, 0, 16, 0)
                background = resources.getDrawable(R.drawable.green_round_bg, null)
                text = i.toString()
                gravity = Gravity.CENTER_VERTICAL
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#FFFFFF"))
            }

            linearLayout.addView(textView)
            //add text view as actionView to menu item
            menuItem.actionView = linearLayout

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

}
