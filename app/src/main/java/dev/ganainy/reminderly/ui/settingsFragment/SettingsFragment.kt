package dev.ganainy.reminderly.ui.settingsFragment

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.ui.baseFragment.ProvideDatabaseViewModelFactory
import dev.ganainy.reminderly.ui.mainActivity.MainActivity
import dev.ganainy.reminderly.utils.ALLOW_PERSISTENT_NOTIFICATION
import dev.ganainy.reminderly.utils.DONE_ACTION_FOR_REMINDERS
import dev.ganainy.reminderly.utils.DONE_ACTION_FOR_REPEATING_REMINDERS
import dev.ganainy.reminderly.utils.MyUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat() {
    //todo finish refactoring
    private lateinit var viewModel: SettingsViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    private val persistentNotificationSwitch by lazy { findPreference<SwitchPreferenceCompat>("persistent_notification") }
    private val dontDisturbSwitch by lazy { findPreference<SwitchPreferenceCompat>("don't_disturb_switch") }
    private val nightModeSwitch by lazy { findPreference<SwitchPreferenceCompat>("night_mode_switch") }
    private val doneBehaviour by lazy { findPreference<Preference>("done_behaviour") }
    private val doneBehaviourForRecurringTasks by lazy { findPreference<Preference>("done_behaviour_for_recurring_tasks") }
    private val dontDisturbValue by lazy { findPreference<Preference>("don't_disturb_value") }

    private lateinit var dontDisturbView: MaterialDialog

    private val disposable by lazy { CompositeDisposable() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViewModel()
        setupSelections()
        viewModel.getCurrentDndPeriod()

        /**triggered by viewmodel to update dnd period on UI*/
        disposable.add(viewModel.dndSubject.subscribe({ dndPeriod ->
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
        }, {
            MyUtils.showCustomToast(requireContext(), R.string.something_went_wrong)
        }))


        /**triggered by viewmodel when it needs UI to show toast*/
        disposable.add(viewModel.toastSubject.subscribe { stringResourceId ->
            MyUtils.showCustomToast(requireContext(), stringResourceId)
        })

        /**triggered by viewmodel to update done behaviour summary */
        disposable.add(viewModel.doneBehaviourSummarySubject.subscribe {doneBehaviourSummary ->
            Timber.d("DebugTag, SettingsFragment->onActivityCreated: ${doneBehaviourSummary}")
          doneBehaviour!!.summary =doneBehaviourSummary
        })

        /**triggered by viewmodel to update done behaviour of repeating tasks summary */
        disposable.add(viewModel.doneBehaviourRepeatingTasksSummarySubject.subscribe {doneBehaviourSummary ->
            doneBehaviourForRecurringTasks!!.summary =doneBehaviourSummary
        })

        /**triggered by viewmodel to recreate activity on night mode change*/
        disposable.add(viewModel.nightModeUpdateSubject.subscribe {isNightModeUpdated ->
            Timber.d("DebugTag, SettingsFragment->onActivityCreated: ${isNightModeUpdated}")
            requireActivity().recreate()
        })

        /**triggered by viewmodel to enable/disable dont disturb switch*/
        disposable.add(viewModel.updateDontDisturbSwitchSubject.subscribe {isDontDisturbEnabled ->
            dontDisturbValue?.isEnabled = isDontDisturbEnabled
        })

        /**triggered by viewmodel to enable/disable persistent notification switch*/
        disposable.add(viewModel.updatePersistentNotificationSwitchSubject.subscribe {isPersistentNotificationEnabled ->
            persistentNotificationSwitch!!.isChecked= isPersistentNotificationEnabled
        })

    }

    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao
        viewModelFactory =
            ProvideDatabaseViewModelFactory(requireActivity().application, reminderDatabaseDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SettingsViewModel::class.java)
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout, rootKey)


        persistentNotificationSwitch!!.setOnPreferenceClickListener {
            if (persistentNotificationSwitch!!.isChecked) {
                //      todo move this observing to a broadcast
                (requireActivity() as MainActivity).observeTodayReminders()
                MyUtils.putInt(requireContext(), ALLOW_PERSISTENT_NOTIFICATION, 0)
            } else {
                MyUtils.cancelPersistentNotification(requireContext())
                MyUtils.putInt(requireContext(), ALLOW_PERSISTENT_NOTIFICATION, 1)
            }
            true
        }

        dontDisturbSwitch?.setOnPreferenceClickListener {
            viewModel.
            updateDontDisturb(dontDisturbSwitch!!.isChecked)
            true
        }


        /**show dialog on done preference click*/
        doneBehaviour?.setOnPreferenceClickListener {
            showDoneBehaviourDialog()
            true
        }

        /**show dialog to change done behaviour for repeating tasks on preference click*/
        doneBehaviourForRecurringTasks?.setOnPreferenceClickListener {
            showDoneBehaviourForRepeatingTasksDialog()
            true
        }


        /**show dialog to change DND period on preference click*/
        dontDisturbValue?.setOnPreferenceClickListener {
            showDontDisturbValueChangeDialog()
            true
        }

        nightModeSwitch?.setOnPreferenceClickListener {
            viewModel.updateNightMode(nightModeSwitch!!.isChecked)
            true
        }

    }

    private fun showDontDisturbValueChangeDialog() {
        MaterialDialog(requireContext()).show {
            dontDisturbView = customView(R.layout.dont_disturb_layout)

            dontDisturbView.findViewById<TextView>(R.id.startTextView).setOnClickListener {
                //user clicked start time text view
                showStartTimePicker()
            }
            dontDisturbView.findViewById<TextView>(R.id.endTextView).setOnClickListener {
                //user clicked end time text view
                showEndTimePicker()
            }
        }
    }

    private fun showDoneBehaviourDialog() {
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.done_behaviour_list,
                initialSelection = MyUtils.getInt(context, DONE_ACTION_FOR_REMINDERS)
            ) { _, index, _ ->
                // Invoked when the user selects an item
                viewModel.updateDoneBehaviourSharedPref(index)
            }
            negativeButton(R.string.cancel)
            positiveButton(R.string.confirm)
            title(0, getString(R.string.done_behaviour_for_recurring_tasks))
        }
    }

    private fun showDoneBehaviourForRepeatingTasksDialog() {
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.done_behaviour_for_recurring_tasks_list
                ,
                initialSelection = MyUtils.getInt(context, DONE_ACTION_FOR_REPEATING_REMINDERS)
            ) { _, index, _ ->
                // Invoked when the user selects an item
                viewModel.updateDoneBehaviourRepeatingTasksSharedPref(index)
            }
            negativeButton(R.string.cancel)
            positiveButton(R.string.confirm)
            title(0, getString(R.string.done_behaviour_for_recurring_tasks))
        }
    }

    private fun showEndTimePicker() {
        //callback triggered when user selects time
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            viewModel.updateDndEnd(hour, min)
        }

        //show dialog to select time
        TimePickerDialog(
            requireActivity(),
            timeSetListener,
            0,
            0,
            false
        ).show()
    }

    private fun showStartTimePicker() {
        //callback triggered when user selects time
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            viewModel.updateDndStart(hour, min)
        }

        //show dialog to select time
        TimePickerDialog(
            requireActivity(),
            timeSetListener,
            0,
            0,
            false
        ).show()
    }

    //setup settings' summaries based on saved values in shared pref
    private fun setupSelections() {

        viewModel.observeDoneBehaviourRepeatingTasksSharedPref()

        viewModel.observeDoneBehaviourSharedPref()

        viewModel.observeAllowPersistentNotificationSharedPref()

        dontDisturbSwitch?.isChecked?.let { viewModel.updateDontDisturbSwitchSubject.onNext(it) }
    }


    override fun onDestroy() {
        super.onDestroy()
       disposable.clear()
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
            Configuration.UI_MODE_NIGHT_NO -> view?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }


        return view
    }

}
