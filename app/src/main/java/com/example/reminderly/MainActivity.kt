package com.example.reminderly

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        setSupportActionBar(binding.toolbar as Toolbar)


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

        //  addItemsToMenu()
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
            val add = menu.add("Runtime item $i")
            add.icon = ResourcesCompat.getDrawable(resources, R.drawable.note_ic, null)

            val textView = TextView(this)
            textView.apply {
                text = i.toString()
                gravity = Gravity.CENTER_VERTICAL
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#4CAF50"))
            }
            add.actionView = textView
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
