package com.example.reminderly

import android.app.Application
import com.example.reminderly.Utils.AD_CLICK_PER_SESSION
import com.example.reminderly.Utils.MyUtils
import com.facebook.stetho.Stetho
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //init this library to show database
        Stetho.initializeWithDefaults(this)
        //reset ad click count with new app session
        MyUtils.putInt(applicationContext, AD_CLICK_PER_SESSION,0)
    }



}