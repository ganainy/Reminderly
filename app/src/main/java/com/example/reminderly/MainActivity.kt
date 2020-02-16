package com.example.reminderly

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.reminderly.Utils.DateUtils
import com.example.reminderly.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    private lateinit var viewModel: MainActivityViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setSupportActionBar(binding.toolbar as Toolbar )


        setupNavControllerWithDrawer()


        //set date in navigation drawer header
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.dateTextView).text =
            DateUtils.getCurrentDateFormatted()

        //open calendar on calendar image click
        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.calendarImageView)
            .setOnClickListener {
                viewModel.navigateToCalendarFragment()
                //close navigation drawer
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }

          //addItemsToMenu()
    }

    private fun setupNavControllerWithDrawer() {

        //setup nav controller with drawer
        val navController = this.findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        //setup action bar with nav controller
        appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun addItemsToMenu() {
        //todo use this to add item programmatically to navigation drawer
        val menu: Menu = nav_view.menu
        for (i in 1..3) {
            val menuItem = menu.add("Runtime item $i")
            menuItem.icon = ResourcesCompat.getDrawable(resources, R.drawable.note_ic, null)

            //create linear layout with textview in middle of it to allign it with menu item
            val linearLayout=LinearLayout(this)
            linearLayout.gravity=Gravity.CENTER

            val textView = TextView(this)
            textView.apply {
                setPadding(16,0,16,0)
                background=resources.getDrawable(R.drawable.green_round_bg,null)
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
