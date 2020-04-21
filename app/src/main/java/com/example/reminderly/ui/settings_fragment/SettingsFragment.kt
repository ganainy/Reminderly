package com.example.reminderly.ui.settings_fragment

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.*
import com.example.reminderly.models.DndPeriod
import com.example.reminderly.ui.mainActivity.MainActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class SettingsFragment : PreferenceFragmentCompat() {


    private val persistentNotificationSwitch by lazy { findPreference<SwitchPreferenceCompat>("persistent_notification") }
    private val dontDisturbSwitch by lazy { findPreference<SwitchPreferenceCompat>("don't_disturb_switch") }
    private val nightModeSwitch by lazy { findPreference<SwitchPreferenceCompat>("night_mode_switch") }
    private val doneBehaviour by lazy { findPreference<Preference>("done_behaviour") }
    private val languagePreference by lazy { findPreference<Preference>("language_pref") }
    private val doneBehaviourForRecurringTasks by lazy { findPreference<Preference>("done_behaviour_for_recurring_tasks") }
    private val dontDisturbValue by lazy { findPreference<Preference>("don't_disturb_value") }

    private val dndTime = DndPeriod()
    private val subject = PublishSubject.create<DndPeriod>()


    private lateinit var dontDisturbView: MaterialDialog


    private val disposable by lazy { CompositeDisposable() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupSelections()


        val observer = object : Observer<DndPeriod> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(dndPeriod: DndPeriod) {
                Log.d("DebugTag", "onNext: $dndPeriod")

                if (::dontDisturbView.isInitialized) {

                    //setup values in views
                    dontDisturbView.findViewById<TextView>(R.id.startTextView).text =
                        MyUtils.formatTime(
                            dndPeriod.startHour,
                            dndPeriod.startMinute
                        )
                    dontDisturbView.findViewById<TextView>(R.id.endTextView).text =
                        MyUtils.formatTime(
                            dndPeriod.endHour,
                            dndPeriod.endMinute
                        )
                }

                //update dontdisturb summary to show the correct duration
                dontDisturbValue?.summary = resources.getString(
                    R.string.dont_disturb_value_summary, MyUtils.formatTime(
                        dndPeriod.startHour,
                        dndPeriod.startMinute
                    ), MyUtils.formatTime(
                        dndPeriod.endHour,
                        dndPeriod.endMinute
                    )
                )

                //save selected start time to shared pref
                MyUtils.putInt(requireContext(), DONT_DISTURB_START_HOURS, dndPeriod.startHour)
                MyUtils.putInt(requireContext(), DONT_DISTURB_START_MINUTES, dndPeriod.startMinute)

                //save selected end time to shared pref
                MyUtils.putInt(requireContext(), DONT_DISTURB_END_HOURS, dndPeriod.endHour)
                MyUtils.putInt(requireContext(), DONT_DISTURB_END_MINUTES, dndPeriod.endMinute)

            }

            override fun onError(e: Throwable) {
                MyUtils.showErrorToast(requireContext())
            }

        }

        subject.subscribe(observer)

        dndTime.startMinute = MyUtils.getInt(requireContext(), DONT_DISTURB_START_MINUTES)
        dndTime.startHour = MyUtils.getInt(requireContext(), DONT_DISTURB_START_HOURS)
        dndTime.endHour = MyUtils.getInt(requireContext(), DONT_DISTURB_END_HOURS)
        dndTime.endMinute = MyUtils.getInt(requireContext(), DONT_DISTURB_END_MINUTES)

        subject.onNext(dndTime)


    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout, rootKey)


        persistentNotificationSwitch!!.setOnPreferenceClickListener {
            if (persistentNotificationSwitch!!.isChecked) {
                (requireActivity() as MainActivity).sendPersistentNotification()
                MyUtils.putInt(requireContext(), ALLOW_PERSISTENT_NOTIFICATION, 0)
            } else {
                (requireActivity() as MainActivity).cancelPersistentNotification()
                MyUtils.putInt(requireContext(), ALLOW_PERSISTENT_NOTIFICATION, 1)
            }
            true
        }

        dontDisturbSwitch?.setOnPreferenceClickListener {
            if (dontDisturbSwitch!!.isChecked) {
                MyUtils.putInt(requireContext(), DND_OPTION_ENABLED, 1)
                dontDisturbValue?.isEnabled = true
            } else {
                MyUtils.putInt(requireContext(), DND_OPTION_ENABLED, 0)
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

                            doneBehaviourForRecurringTasks!!.summary =
                                MyUtils.getStringFromResourceArray(
                                    context,
                                    R.array.done_behaviour_for_recurring_tasks_list, 0
                                )

                        }
                        1 -> {
                            MyUtils.putInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS, 1)

                            doneBehaviourForRecurringTasks!!.summary =
                                MyUtils.getStringFromResourceArray(
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


            MaterialDialog(requireContext()).show {
                dontDisturbView = customView(R.layout.dont_disturb_layout)
                subject.onNext(dndTime)


                dontDisturbView.findViewById<TextView>(R.id.startTextView).setOnClickListener {
                    //user clicked start time text view
                    showStartTimePicker()
                }
                dontDisturbView.findViewById<TextView>(R.id.endTextView).setOnClickListener {
                    //user clicked start time text view
                    showEndTimePicker()
                }
            }



            true
        }

        nightModeSwitch?.setOnPreferenceClickListener {
            if (nightModeSwitch!!.isChecked) {
                MyUtils.putInt(requireContext(), NIGHT_MODE_ENABLED, 1)
                requireActivity().recreate()
            } else {
                MyUtils.putInt(requireContext(), NIGHT_MODE_ENABLED, 0)
                requireActivity().recreate()
            }
            true
        }

        languagePreference?.setOnPreferenceClickListener {
            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    R.array.language_options_list
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    //update shared pref value , setting summary based on user selection
                    when (index) {
                        0 -> {
                            if (   MyUtils.getInt(context, APP_LANGUAGE)==0){
                                MyUtils.showCustomToast(requireContext(),R.string.this_option_already_active)
                            }else{
                                MyUtils.putInt(context, APP_LANGUAGE, 0)
                                MyUtils.showCustomToast(requireContext(),R.string.changes_take_effect_on_restart)
                            }
                        }
                        1 -> {
                            if (   MyUtils.getInt(context, APP_LANGUAGE)==1){
                                MyUtils.showCustomToast(requireContext(),R.string.this_option_already_active)
                            }else{
                                MyUtils.putInt(context, APP_LANGUAGE, 1)
                                MyUtils.showCustomToast(requireContext(),R.string.changes_take_effect_on_restart)
                            }
                        }
                        2 -> {
                            if (   MyUtils.getInt(context, APP_LANGUAGE)==2){
                                MyUtils.showCustomToast(requireContext(),R.string.this_option_already_active)
                            }else{
                                MyUtils.putInt(context, APP_LANGUAGE, 2)
                                MyUtils.showCustomToast(requireContext(),R.string.changes_take_effect_on_restart)
                            }
                        }
                    }


                }
                negativeButton(R.string.cancel)
                positiveButton(R.string.confirm)
                title(0, getString(R.string.choose_language))
            }
            true
        }
    }

    private fun showEndTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            if (validateEndTime(hour, min)) {
                dndTime.endHour = hour
                dndTime.endMinute = min
                subject.onNext(dndTime)
            }
        }

        TimePickerDialog(
            requireActivity(), timeSetListener, 0, 0, false
        ).show()
    }

    private fun showStartTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            if (validateStartTime(hour, min)) {
                dndTime.startHour = hour
                dndTime.startMinute = min
                subject.onNext(dndTime)
            }
        }


        TimePickerDialog(
            requireActivity(), timeSetListener, 0, 0, false
        ).show()
    }

    /**check that the selected start time is less than the selected/saved end time*/
    private fun validateStartTime(hour: Int, min: Int): Boolean {
        when {
            hour < MyUtils.getInt(requireContext(), DONT_DISTURB_END_HOURS) -> {
                return true
            }
            hour == MyUtils.getInt(requireContext(), DONT_DISTURB_END_HOURS) -> {
                when {
                    min < MyUtils.getInt(requireContext(), DONT_DISTURB_END_MINUTES) -> {
                        return true
                    }
                    else -> {
                        MyUtils.showCustomToast(requireContext(), R.string.wrong_dnd_period)

                    }
                }
            }
            else -> {
                MyUtils.showCustomToast(requireContext(), R.string.wrong_dnd_period)

            }
        }
        return false
    }

    /**check that the selected end time is higher than the selected/saved start time*/
    private fun validateEndTime(hour: Int, min: Int): Boolean {
        when {
            hour > MyUtils.getInt(requireContext(), DONT_DISTURB_START_HOURS) -> {
                return true
            }
            hour == MyUtils.getInt(requireContext(), DONT_DISTURB_START_HOURS) -> {
                when {
                    min > MyUtils.getInt(requireContext(), DONT_DISTURB_START_MINUTES) -> {
                        return true
                    }
                    else -> {
                        MyUtils.showCustomToast(requireContext(), R.string.wrong_dnd_period)

                    }
                }
            }
            else -> {
                MyUtils.showCustomToast(requireContext(), R.string.wrong_dnd_period)

            }
        }
        return false
    }

    private fun deleteExistingDoneReminders() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao
        disposable.add(reminderDatabaseDao.getDoneReminders().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { doneReminderList ->
                Log.d("DebugTag", "deleteExistingDoneReminders: ${doneReminderList.size}")
                for (doneReminder in doneReminderList) {
                    reminderDatabaseDao.delete(doneReminder).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({
                            Log.d("DebugTag", "deleteExistingDoneReminders: complete")
                        },
                            { error ->
                                Log.d(
                                    "DebugTag",
                                    "deleteExistingDoneReminders: error ${error.message}"
                                )
                            })
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

        /**--*/

        when (dontDisturbSwitch?.isChecked) {
            true -> {
                dontDisturbValue?.isEnabled = true
            }
            false -> {
                dontDisturbValue?.isEnabled = false
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**override background color to white in day mode and grey in night mode*/
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val nightModeFlags = context!!.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> view?.setBackgroundColor(Color.parseColor("#5B5B5B"))
            Configuration.UI_MODE_NIGHT_NO ->  view?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }


        return view
    }

}
