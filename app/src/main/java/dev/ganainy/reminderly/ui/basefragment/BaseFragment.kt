package dev.ganainy.reminderly.ui.basefragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.example.footy.database.ReminderDatabase
import com.example.ourchat.ui.chat.ReminderAdapter
import com.example.ourchat.ui.chat.ReminderClickListener
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.Utils.MyUtils
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.ui.mainActivity.ICommunication


open class BaseFragment : Fragment() {

    lateinit var adapter: ReminderAdapter
    private lateinit var viewModel: BaseFragmentViewModel
    private lateinit var viewModelFactory: ProvideDatabaseViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_base, container, false)
    }

    companion object {
        fun newInstance() = BaseFragment()
    }

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViewModel()

        /**triggered by viewmodel on reminder update*/
        viewModel.updatePositionSubject.subscribe{ position ->
            adapter.notifyItemChanged(position)
        }

        /**triggered by viewmodel when it needs to show some toast*/
        viewModel.toastSubject.subscribe {stringResourceId ->
           MyUtils.showCustomToast(requireContext(),stringResourceId)
        }

        /**triggered by viewmodel when it needs to cancel alarm related to certain reminder*/
        viewModel.cancelAlarmSubject.subscribe{reminderToCancelAlarmOf ->
            MyUtils.cancelAlarmManager(reminderToCancelAlarmOf, requireContext())
        }

        /**triggered by viewmodel when it needs to add alarm related to certain reminder*/
        viewModel.addAlarmSubject.subscribe { reminderToAddAlarmTo ->
            MyUtils.addAlarmManager(reminderToAddAlarmTo,requireContext())

        }


    }

    private fun initViewModel() {
        val reminderDatabaseDao =  ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao

        viewModelFactory =

                ProvideDatabaseViewModelFactory(
                    requireActivity().application,
                    reminderDatabaseDao
                )

        viewModel = ViewModelProvider(this, viewModelFactory).get(BaseFragmentViewModel::class.java)
    }

    fun initAdapter() {
        adapter = ReminderAdapter(requireActivity(), object : ReminderClickListener {
            override fun onReminderClick(reminder: Reminder) {
                editReminder(reminder)
            }

            override fun onFavoriteClick(reminder: Reminder, position: Int) {
                viewModel.updateReminderFavorite(reminder,position)
            }

            override fun onMenuClick(reminder: Reminder,position: Int) {
                showOptionsSheet(reminder,position)
            }

        })
    }

    private fun showOptionsSheet(
        reminder: Reminder,
        position: Int
    ) {
        val items = listOf(
            BasicGridItem(R.drawable.ic_check_grey, getString(R.string.done)),
            BasicGridItem(R.drawable.ic_access_time_grey, getString(R.string.postpone)),
            BasicGridItem(R.drawable.ic_edit_grey, getString(R.string.edit)),
            BasicGridItem(R.drawable.ic_content_copy_grey, getString(R.string.mcopy)),
            BasicGridItem(R.drawable.ic_share_grey, getString(R.string.share)),
            BasicGridItem(R.drawable.ic_delete_grey, getString(R.string.delete))
        )

        MaterialDialog(requireActivity(), BottomSheet()).show {
            gridItems(items) { _, index, item ->
                when (index) {
                    0 -> {
                        viewModel.markReminderAsDone(reminder,position)
                    }
                    1 -> {
                        postponeReminder(reminder,position)
                    }
                    2 -> {
                        editReminder(reminder)
                    }
                    3 -> {
                        copyToClipboard(reminder)
                    }
                    4->{
                        shareReminder(reminder)
                    }
                    5->{
                        viewModel.deleteReminder(reminder,position)
                    }

                }
            }
        }
    }



    private fun shareReminder(reminder: Reminder) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reminder.text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun copyToClipboard(reminder: Reminder) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", reminder.text)
        clipboard.setPrimaryClip(clip)
        MyUtils.showCustomToast(requireContext(),R.string.copied_to_clipboard)
    }

    private fun editReminder(reminder: Reminder) {
        (requireActivity() as? ICommunication)?.showReminderFragment(reminder)
    }


    private fun postponeReminder(
        reminder: Reminder,
        position: Int
    ) {

        //those variables to reflect the change in pickers
        var minute = 0
        var hour = 0
        var day = 0


        val dialog = MaterialDialog(requireContext()).show {
            customView(R.layout.custom_postpone_dialog)
            positiveButton(R.string.confirm) {
             viewModel.postponeReminder(reminder,day,hour,minute,position)
            }
            negativeButton(R.string.cancel)
            title(0, getString(R.string.select_time))
        }

        //get value of minutePicker
        dialog.getCustomView().findViewById<NumberPicker>(R.id.minutePicker).apply {
            maxValue = 59
            minValue = 0
            setOnValueChangedListener { picker, oldVal, newVal ->
                minute = newVal
            }
        }

        //get value of hourPicker
        dialog.getCustomView().findViewById<NumberPicker>(R.id.hourPicker).apply {
            maxValue = 23
            minValue = 0
            setOnValueChangedListener { picker, oldVal, newVal ->
                hour = newVal
            }
        }

        //get value of datePicker
        dialog.getCustomView().findViewById<NumberPicker>(R.id.dayPicker).apply {
            minValue = 0
            maxValue = 30
            setOnValueChangedListener { picker, oldVal, newVal ->
                day = newVal
            }
        }


    }

}
