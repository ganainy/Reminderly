package com.example.reminderly.ui.basefragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.example.footy.database.ReminderDatabase
import com.example.ourchat.ui.chat.ReminderAdapter
import com.example.ourchat.ui.chat.ReminderClickListener
import com.example.reminderly.R
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.ui.mainActivity.ICommunication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


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
        adapter = ReminderAdapter(requireActivity(), object : ReminderClickListener {
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
            BasicGridItem(R.drawable.ic_content_copy_grey, getString(R.string.copy)),
            BasicGridItem(R.drawable.ic_share_grey, getString(R.string.share)),
            BasicGridItem(R.drawable.ic_delete_grey, getString(R.string.delete))
        )

        MaterialDialog(requireActivity(), BottomSheet()).show {
            gridItems(items) { _, index, item ->
                when (index) {
                    0 -> {
                        /**make reminder done and update in db && show toast*/
                        handleDoneClick(reminder)
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
                        deleteReminder(reminder)
                    }

                }
            }
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        disposable.add(viewModel.deleteReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { Toast.makeText(context,getString(R.string.reminder_deleted), Toast.LENGTH_SHORT).show()
                //cancel alarm of this reminder
                MyUtils.cancelAlarm(reminder.id,context)
            },{
                    error->
                (Toast.makeText(context,getString(R.string.reminder_delete_failed), Toast.LENGTH_SHORT).show())
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

               val postponedReminder= MyUtils.postponeReminder(reminder,context, day, hour,minute )
                if (postponedReminder==null){
                    //postpone failed do nothing
                }else{
                    disposable.add(viewModel.updateReminder(postponedReminder).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            // set alarm
                            MyUtils.addAlarm(reminder.id,context,reminder.createdAt.timeInMillis,reminder.repeat)
                            adapter.notifyItemChanged(position)

                            Toast.makeText(requireActivity(), getString(R.string.reminder_postponed), Toast.LENGTH_SHORT).show()
                        })
                }


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




    private fun handleDoneClick(reminder: Reminder) {
        reminder.isDone = true
        disposable.add(viewModel.updateReminder(reminder).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                MyUtils.cancelAlarm(reminder.id,context)
                Toast.makeText(requireActivity(), getString(R.string.marked_as_done), Toast.LENGTH_SHORT).show()
            })
    }


    override fun onStop() {
        super.onStop()
        disposable.clear()

    }





}
