package com.example.reminderly.ui.basefragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import com.example.ourchat.ui.chat.ReminderAdapter
import com.example.ourchat.ui.chat.ReminderClickListener
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.mainActivity.ICommunication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


open class BaseFragment : Fragment() {

    lateinit var adapter: ReminderAdapter
    private val disposable = CompositeDisposable()
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
        fun newInstance(param1: String, param2: String) = BaseFragment()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao

        viewModelFactory =
            ProvideDatabaseViewModelFactory(
                requireActivity().application,
                reminderDatabaseDao
            )

        viewModel = ViewModelProvider(this, viewModelFactory).get(BaseFragmentViewModel::class.java)
    }

    fun initAdapter() {
        adapter = ReminderAdapter(requireContext(), object : ReminderClickListener {
            override fun onReminderClick(reminder: Reminder) {
                editReminder(reminder)
            }

            override fun onFavoriteClick(reminder: Reminder, position: Int) {
                reminder.isFavorite =
                    !reminder.isFavorite //change favorite value then update in database
                disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { /*task completed*/ })

                adapter.notifyItemChanged(position)
            }

            override fun onMenuClick(reminder: Reminder) {

                showOptionsSheet(reminder)
            }

        })
    }

    private fun showOptionsSheet(reminder: Reminder) {
        val items = listOf(
            BasicGridItem(R.drawable.ic_check_grey, getString(R.string.done)),
            BasicGridItem(R.drawable.ic_access_time_grey, getString(R.string.postpone)),
            BasicGridItem(R.drawable.ic_edit_grey, getString(R.string.edit)),
            BasicGridItem(R.drawable.ic_content_copy_grey, getString(R.string.copy)),
            BasicGridItem(R.drawable.ic_share_grey, getString(R.string.share)),
            BasicGridItem(R.drawable.ic_delete_grey, getString(R.string.delete))
        )

        MaterialDialog(requireContext(), BottomSheet()).show {
            gridItems(items) { _, index, item ->
                when (index) {
                    0 -> {
                        /**make reminder done and update in db && show toast*/
                        handleDoneClick(reminder)
                    }
                    1 -> {
                        postponeReminder(reminder)
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
                        deleteReminder(reminder)
                    }

                }
            }
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        disposable.add(viewModel.deleteReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { Toast.makeText(context,getString(R.string.reminder_deleted), Toast.LENGTH_SHORT).show() },{
                    error->
                (Toast.makeText(context,getString(R.string.reminder_delete_failed), Toast.LENGTH_SHORT).show())
                Log.d("DebugTag", "deleteReminder: "+error.message)
            }))

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

    private fun MaterialDialog.copyToClipboard(reminder: Reminder) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", reminder.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT
        )
            .show()
    }

    private fun editReminder(reminder: Reminder) {
        (requireActivity() as ICommunication).showReminderFragment(reminder)
    }

    private fun postponeReminder(reminder: Reminder) {

        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.postpone_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                when (index) {
                    0 -> {
                        postpone(reminder, 5)
                    }
                    1 -> {
                        postpone(reminder, 15)
                    }
                    2 -> {
                        postpone(reminder, 30)
                    }
                    3 -> {
                        postpone(reminder, 60)
                    }
                    4 -> showCustomPostponeDialog(reminder)
                }

            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.postpone_time))

        }

    }

    private fun showCustomPostponeDialog(reminder: Reminder) {

        //those variables to reflect the change in pickers
        var minute = 0
        var hour = 0
        var day = 0


        val dialog = MaterialDialog(requireContext()).show {
            customView(R.layout.custom_postpone_dialog)
            positiveButton(R.string.confirm) {
                /**On confirm click will first check if user postponed at least one min */
                if (minute + hour + day == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.atleast_one_minute),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@positiveButton
                }


                postpone(reminder, minute, hour, day)


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


    private fun postpone(reminder: Reminder, minute: Int, hour: Int = 0, day: Int = 0) {

        /** will check that the new reminder date is bigger than current date; because its useless
         *  to postpone reminder to a previous date*/

        reminder.createdAt.apply {
            add(Calendar.MINUTE, minute)
            add(Calendar.HOUR_OF_DAY, hour)
            add(Calendar.DAY_OF_MONTH, day)
        }


        if (reminder.createdAt.before(Calendar.getInstance())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.must_be_upcoming_date),
                Toast.LENGTH_LONG
            )
                .show()
            /*reminder should return to original value since update won't be saved*/
            reminder.createdAt.apply {
                add(Calendar.MINUTE, -minute)
                add(Calendar.HOUR_OF_DAY, -hour)
                add(Calendar.DAY_OF_MONTH, -day)
            }
            return
        }

        disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.reminder_postponed),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
    }


    private fun handleDoneClick(reminder: Reminder) {
        reminder.isDone = true
        disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.marked_as_done),
                    Toast.LENGTH_SHORT
                )
                    .show()
            })
    }


    override fun onStop() {
        super.onStop()
        disposable.clear()

    }





}
