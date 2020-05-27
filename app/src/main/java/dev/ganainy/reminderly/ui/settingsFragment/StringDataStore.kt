package dev.ganainy.reminderly.ui.settingsFragment

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore


class StringDataStore(val context: Context) : PreferenceDataStore() {

    private val pref: SharedPreferences = context.applicationContext
        .getSharedPreferences("MyPref", 0)

    override fun putString(key: String, value: String?) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    override fun getString(key: String, defValue: String?): String? {
       return pref.getString(key, null)
    }
}