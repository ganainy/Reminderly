package com.example.reminderly

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.reminderly.Utils.DateUtils
import com.example.reminderly.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration:AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        supportActionBar?.elevation=0f


        //setup nav controller with drawer
        val navController = this.findNavController(R.id.nav_host_fragment)
         appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)

        //setup action bar with nav controller
        setupActionBarWithNavController(navController, appBarConfiguration)

        //set date in navigation drawer header
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.dateTextView).text= DateUtils.getCurrentDateFormatted()
        
        //open calendar on calendar image click
        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.calendarImageView).setOnClickListener {
            Log.d("DebugTag", "onCreate: ")
        }

      //  addItemsToMenu()
    }

    private fun addItemsToMenu() {
        //todo use this to add item programmatically to navigation drawer
        val menu: Menu = nav_view.menu
        for (i in 1..3) {
            val add = menu.add("Runtime item $i")
            add.icon=ResourcesCompat.getDrawable(resources, R.drawable.note_ic, null)

            val textView =TextView(this)
            textView.apply {
                text=i.toString()
                gravity = Gravity.CENTER_VERTICAL
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#4CAF50"))
            }
            add.actionView=textView
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
