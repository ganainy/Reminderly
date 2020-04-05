package com.example.reminderly.ui.settings_fragment

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.ALLOW_PERSISTENT_NOTIFICATION
import com.example.reminderly.Utils.DONE_ACTION_FOR_REMINDERS
import com.example.reminderly.Utils.DONE_ACTION_FOR_REPEATING_REMINDERS
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.ui.mainActivity.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class SettingsFragment : PreferenceFragmentCompat() {

    private val doneBehaviourDataStore by lazy { StringDataStore(requireContext()) }
    private val doneBehaviourForRecurringTasksDataStore by lazy { StringDataStore(requireContext()) }
    private val dontDisturbDataStore by lazy { StringDataStore(requireContext()) }

    private val persistentNotificationSwitch by lazy { findPreference<SwitchPreferenceCompat>("persistent_notification") }
    private val dontDisturbSwitch by lazy { findPreference<SwitchPreferenceCompat>("don't_disturb_switch") }
    private val doneBehaviour by lazy { findPreference<Preference>("done_behaviour") }
    private val doneBehaviourForRecurringTasks by lazy { findPreference<Preference>("done_behaviour_for_recurring_tasks") }
    private val dontDisturbValue by lazy { findPreference<Preference>("don't_disturb_value") }

    private val disposable by lazy { CompositeDisposable() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupDataStores()
        setupSelections()

    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout, rootKey)


        //                doneBehaviourDataStore.putString("summary","sax")
        //val string1 = doneBehaviourDataStore.getString("summary", null)


        persistentNotificationSwitch!!.setOnPreferenceClickListener {
            if (persistentNotificationSwitch!!.isChecked) {
                (requireActivity() as MainActivity).sendPersistentNotification()
                MyUtils.putInt(requireContext(),ALLOW_PERSISTENT_NOTIFICATION,0)
            } else {
                (requireActivity() as MainActivity).cancelPersistentNotification()
                MyUtils.putInt(requireContext(),ALLOW_PERSISTENT_NOTIFICATION,1)
            }
            true
        }

        dontDisturbSwitch?.setOnPreferenceClickListener {
            if (dontDisturbSwitch!!.isChecked) {
                //todo
                dontDisturbValue?.isEnabled = true
            } else {
                //todo
                dontDisturbValue?.isEnabled = false
            }
            true
        }

        doneBehaviour?.setOnPreferenceClickListener {

            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    R.array.done_behaviour_list
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    //update shared pref value , setting summary based on user selection
                    when (index) {
                        0 -> {
                            MyUtils.putInt(context, DONE_ACTION_FOR_REMINDERS, 0)

                            doneBehaviour!!.summary = MyUtils.getStringFromResourceArray(
                                context,
                                R.array.done_behaviour_list, 0
                            )

                        }
                        1 -> {
                            MyUtils.putInt(context, DONE_ACTION_FOR_REMINDERS, 1)

                            doneBehaviour!!.summary = MyUtils.getStringFromResourceArray(
                                context,
                                R.array.done_behaviour_list, 1
                            )

                            deleteExistingDoneReminders()

                        }
                    }
                }
                negativeButton(R.string.cancel)
                positiveButton(R.string.confirm)
                title(0, getString(R.string.done_behaviour_for_recurring_tasks))
            }
            true

            true
        }

        doneBehaviourForRecurringTasks?.setOnPreferenceClickListener {

            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    R.array.done_behaviour_for_recurring_tasks_list
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    //update shared pref value , setting summary based on user selection
                    when (index) {
                        0 -> {
                            MyUtils.putInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS, 0)

                            doneBehaviourForRecurringTasks!!.summary = MyUtils.getStringFromResourceArray(
                                context,
                                R.array.done_behaviour_for_recurring_tasks_list, 0
                            )

                        }
                        1 -> {
                            MyUtils.putInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS, 1)

                            doneBehaviourForRecurringTasks!!.summary = MyUtils.getStringFromResourceArray(
                                context,
                                R.array.done_behaviour_for_recurring_tasks_list, 1
                            )
                        }
                    }
                }
                negativeButton(R.string.cancel)
                positiveButton(R.string.confirm)
                title(0, getString(R.string.done_behaviour_for_recurring_tasks))
            }
            true
        }


        dontDisturbValue?.setOnPreferenceClickListener {
            //todo
            true
        }

    }

    private fun deleteExistingDoneReminders() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao
        disposable.add(reminderDatabaseDao.getDoneReminders().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { doneReminderList ->
                    Log.d("DebugTag", "deleteExistingDoneReminders: ${doneReminderList.size}")
                    for (doneReminder in doneReminderList){
                        reminderDatabaseDao.delete(doneReminder).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                            Log.d("DebugTag", "deleteExistingDoneReminders: complete")},
                            { error ->Log.d("DebugTag", "deleteExistingDoneReminders: error ${error.message}")})
                    }
                })
    }

    //setup settings' summaries based on saved values in shared pref
    private fun setupSelections() {

        val doneBehaviourForRecurringTasksValue =
            MyUtils.getInt(requireContext(), DONE_ACTION_FOR_REPEATING_REMINDERS)
        when (doneBehaviourForRecurringTasksValue) {
            0 -> {
                doneBehaviourForRecurringTasks?.summary = MyUtils.getStringFromResourceArray(
                    requireContext(),
                    R.array.done_behaviour_for_recurring_tasks_list, 0
                )
            }
            1 -> {
                doneBehaviourForRecurringTasks?.summary = MyUtils.getStringFromResourceArray(
                    requireContext(),
                    R.array.done_behaviour_for_recurring_tasks_list, 1
                )
            }
        }
        /**--*/

        val doneBehaviourValue =
            MyUtils.getInt(requireContext(), DONE_ACTION_FOR_REMINDERS)
        when (doneBehaviourValue) {
            0 -> {
                doneBehaviour?.summary = MyUtils.getStringFromResourceArray(
                    requireContext(),
                    R.array.done_behaviour_list, 0
                )
            }
            1 -> {
                doneBehaviour?.summary = MyUtils.getStringFromResourceArray(
                    requireContext(),
                    R.array.done_behaviour_list, 1
                )
            }
        }

    }

    private fun setupDataStores() {
        val doneBehaviourPreference: Preference? = findPreference("done_behaviour")
        doneBehaviourPreference?.preferenceDataStore = doneBehaviourDataStore

        val doneBehaviourForRecurringTasksPreference: Preference? =
            findPreference("done_behaviour_for_recurring_tasks")
        doneBehaviourForRecurringTasksPreference?.preferenceDataStore =
            doneBehaviourForRecurringTasksDataStore

        val dontDisturbPreference: Preference? = findPreference("don't_disturb_value")
        dontDisturbPreference?.preferenceDataStore = dontDisturbDataStore
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**override background color*/
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        return view
    }

}
