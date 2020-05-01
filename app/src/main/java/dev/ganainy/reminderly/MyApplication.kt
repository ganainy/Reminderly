package dev.ganainy.reminderly

import android.app.Application
import com.facebook.stetho.Stetho
import dev.ganainy.reminderly.Utils.AD_CLICK_PER_SESSION
import dev.ganainy.reminderly.Utils.MyUtils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //init this library to show database
        Stetho.initializeWithDefaults(this)
        //reset ad click count with new app session
        MyUtils.putInt(applicationContext, AD_CLICK_PER_SESSION,0)
    }



}