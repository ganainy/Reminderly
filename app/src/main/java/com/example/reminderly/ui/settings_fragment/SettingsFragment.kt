package com.example.reminderly.ui.settings_fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import com.example.reminderly.R


class SettingsFragment : PreferenceFragmentCompat(){

    private val doneBehaviourDataStore by lazy {  StringDataStore(requireContext()) }
    private val doneBehaviourForRecurringTasksDataStore by lazy {  StringDataStore(requireContext()) }
    private val dontDisturbDataStore by lazy {  StringDataStore(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout, rootKey)

        setupDataStores()

        //                doneBehaviourDataStore.putString("summary","sax")
        //val string1 = doneBehaviourDataStore.getString("summary", null)

        val persistentNotificationSwitch=findPreference<SwitchPreferenceCompat>("persistent_notification")
        val dontDisturbSwitch=findPreference<SwitchPreferenceCompat>("don't_disturb_switch")
        val doneBehaviour=findPreference<Preference>("done_behaviour")
        val doneBehaviourForRecurringTasks=findPreference<Preference>("done_behaviour_for_recurring_tasks")
        val dontDisturbValue=findPreference<Preference>("don't_disturb_value")

        persistentNotificationSwitch?.setOnPreferenceClickListener {
            if (persistentNotificationSwitch.isChecked){
                //todo
            }else{
                //todo
            }
            true
        }

        dontDisturbSwitch?.setOnPreferenceClickListener {
            if (dontDisturbSwitch.isChecked){
                //todo
                dontDisturbValue?.isEnabled=true
            }else{
                //todo
                dontDisturbValue?.isEnabled=false
            }
            true
        }

        doneBehaviour?.setOnPreferenceClickListener {

            //todo
            true
        }

        doneBehaviourForRecurringTasks?.setOnPreferenceClickListener {
            //todo
            true
        }


        dontDisturbValue?.setOnPreferenceClickListener {
            //todo
            true
        }

    }

    private fun setupDataStores() {
        val doneBehaviourPreference: Preference? = findPreference("done_behaviour")
        doneBehaviourPreference?.preferenceDataStore = doneBehaviourDataStore

        val doneBehaviourForRecurringTasksPreference: Preference? = findPreference("done_behaviour_for_recurring_tasks")
        doneBehaviourForRecurringTasksPreference?.preferenceDataStore = doneBehaviourForRecurringTasksDataStore

        val dontDisturbPreference: Preference? = findPreference("don't_disturb_value")
        dontDisturbPreference?.preferenceDataStore = dontDisturbDataStore
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**override background color*/
        val  view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        return view
    }

}
