package dev.ganainy.reminderly.miscellaneous

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.facebook.stetho.Stetho
import dev.ganainy.reminderly.utils.AD_CLICK_PER_SESSION
import dev.ganainy.reminderly.utils.MyUtils
import dev.ganainy.reminderly.broadcast_receivers.BootCompletedIntentReceiver
import timber.log.Timber

const val RESTART_ALARAMS = "restartAlarms"

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //init this library to show database
        Stetho.initializeWithDefaults(this)
        //reset ad click count with new app session
        MyUtils.putInt(applicationContext, AD_CLICK_PER_SESSION, 0)
        //init timber
        Timber.plant(Timber.DebugTree())

        //ignore battery optimization
        requestIgnoreBatteryOptimization()

        //alarms are deleted when app is swiped from recent so we add them again on application creation
        val restartAlarmIntent = Intent(this, BootCompletedIntentReceiver::class.java)
        restartAlarmIntent.putExtra(RESTART_ALARAMS, "")
        sendBroadcast(restartAlarmIntent)

        val rec = MyUtils.getInt(this, "rec")
        val service = MyUtils.getInt(this, "service")
        Timber.d("Timber, receiver calls $rec ....sercvice calls $service")
    }

    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }


}