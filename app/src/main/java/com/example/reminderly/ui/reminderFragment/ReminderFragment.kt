package com.example.reminderly.ui.reminderFragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.footy.database.ReminderDatabase
import com.example.reminderly.R
import com.example.reminderly.Utils.FIRST_TIME_ADD_REMINDER
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ReminderFragmentBinding
import com.example.reminderly.ui.mainActivity.ICommunication
import com.example.reminderly.ui.reminderActivity.ReminderViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.reminder_fragment.*
import java.util.*

const val SPEECH_TO_TEXT_CODE = 1
const val SELECT_PHONE_NUMBER = 2

/**used to create new reminders or edit existing ones*/
class ReminderFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var viewModelFactory: ReminderViewModelFactory
    private lateinit var binding: ReminderFragmentBinding
    private val disposable = CompositeDisposable()
    private var reminder=Reminder()
    private lateinit var reminderObserver:Observer<Reminder>
    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->


           updateReminderDate(
                year = year,
                month = monthOfYear,
                day = dayOfMonth
            )

        }

    companion object {

        fun newInstance(reminder: Reminder): ReminderFragment {
            val fragment = ReminderFragment()
            val args = Bundle()
            args.putParcelable("reminder", reminder)
            fragment.arguments = args

            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.reminder_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

         reminderObserver = object : Observer<Reminder>{
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Reminder) {
                initViews()
            }

            override fun onError(e: Throwable) {
            }

        }


        if (arguments?.get("reminder") != null) {  //navigating from reminders list fragment
             reminder = arguments?.get("reminder") as Reminder
            }
        reminderObserver.onNext(reminder)

        initViewModel()
        setupListeners()
        showHints()


        binding.reminderEditText.addTextChangedListener {
            reminder.text=it.toString()
        }


        initializeAds()

    }


    private fun initializeAds() {
        MobileAds.initialize(requireContext()
        ) {}

        val adRequest: AdRequest = AdRequest.Builder().build()
        binding.adView?.loadAd(adRequest)
    }


    private fun initViewModel() {
        val reminderDatabaseDao = ReminderDatabase.getInstance(requireContext()).reminderDatabaseDao
        viewModelFactory =
            ReminderViewModelFactory(requireActivity().application, reminder, reminderDatabaseDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ReminderViewModel::class.java)
    }


    /**setup reminder type image based on its value*/
    private fun setupReminderTypeImage(reminderType: Int) {
        if (reminderType == 0) {
            binding.reminderTypeImage.setImageResource(R.drawable.ic_notification)
        } else {
            binding.reminderTypeImage.setImageResource(R.drawable.ic_bell_white)
        }
    }

    /**set no repeat image or repeat image depending on repeat value*/
    private fun setupRepeatImage(repeat: Int) {
        if (repeat == 0) {
            binding.repeatImage.setImageResource(R.drawable.ic_no_repeat)
        } else {
            binding.repeatImage.setImageResource(R.drawable.ic_repeat)
        }
    }

    /**setup priority image bg color depending on its value*/
    private fun setupPriorityBackgroundColor(priority: Int) {
        binding.priorityImage.background = when (priority) {
            0 -> {
                resources.getDrawable(R.drawable.darker_green_round_bg, null)
            }
            1 -> {
                resources.getDrawable(R.drawable.yellow_round_bg, null)
            }
            2 -> {
                resources.getDrawable(R.drawable.red_round_bg, null)
            }
            else -> {
                throw Exception("unknown priority value")
            }
        }
    }


    private fun setupListeners() {
        binding.dateImage.setOnClickListener(this)
        binding.dateText.setOnClickListener(this)
        binding.timeImage.setOnClickListener(this)
        binding.timeText.setOnClickListener(this)
        binding.repeatImage.setOnClickListener(this)
        binding.repeatText.setOnClickListener(this)
        binding.priorityImage.setOnClickListener(this)
        binding.priorityText.setOnClickListener(this)
        binding.reminderTypeImage.setOnClickListener(this)
        binding.reminderTypeText.setOnClickListener(this)
        binding.micImage.setOnClickListener(this)
        binding.contactsImage.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        binding.saveFab.setOnClickListener(this)
        binding.keyboardImage.setOnClickListener(this)
    }



    private fun initViews() {
        binding.reminderEditText.setText(reminder.text)
        binding.dateText.text = MyUtils.formatDate(Date(reminder.createdAt.timeInMillis))
        binding.timeText.text = MyUtils.formatTime(Date(reminder.createdAt.timeInMillis))
        binding.repeatText.text = MyUtils.convertRepeat(requireContext(),reminder.repeat)

        binding.priorityText.text = MyUtils.convertPriority(requireContext(),reminder.priority)
        binding.reminderTypeText.text = MyUtils.convertReminderType(requireContext(),reminder.reminderType)
        /**make numbers in edit text clickable & navigate to phone pad on click*/
        Linkify.addLinks(binding.reminderEditText, Linkify.PHONE_NUMBERS)

        setupPriorityBackgroundColor(reminder.priority)
        setupRepeatImage(reminder.repeat)
        setupReminderTypeImage(reminder.reminderType)
    }


    private fun changeKeyboardVisibility() {
        val hideKeyboard = MyUtils.hideKeyboard(requireActivity(), binding.keyboardImage)
        if (hideKeyboard == null || !hideKeyboard) {
            //keyboard was already hidden so we need to show it
            MyUtils.showKeyboard(requireContext())
        }
    }

    private fun pickNumberFromContacts() {
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, SELECT_PHONE_NUMBER)

    }


    private fun openSpeechToTextDialog() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(
                intent,
                SPEECH_TO_TEXT_CODE
            )
        } else {
            MyUtils.showCustomToast(requireActivity(), R.string.feature_not_supported)

        }
    }


    private fun handleReminderTypeImageClick() {
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.notify_type_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                binding.reminderTypeText.text = text
               reminder.reminderType=index
                reminderObserver.onNext(reminder)
                setupReminderTypeImage(index)
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.notification_type))

        }
    }

    private fun handlePriorityImageClick() {
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.priority_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                reminder.priority=index
                reminderObserver.onNext(reminder)
                binding.priorityText.text = text
                setupPriorityBackgroundColor(index)
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.priority_type))

        }
    }

    private fun handleRepeatImageClick() {
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                R.array.repeat_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                binding.repeatText.text = text
                reminder.repeat=index
                reminderObserver.onNext(reminder)
                setupRepeatImage(index)
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.repeat_count))
        }

    }

    private fun handleTimeImageClick() {

        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, min ->

                updateReminderTime(
                    hour = hour,
                    minute = min
                )
            }

        //open date picker
        TimePickerDialog(
            requireActivity(), timeSetListener,
            // set DatePickerDialog to point to reminder's date when it loads up
            reminder.createdAt.get(Calendar.HOUR_OF_DAY),
            reminder.createdAt.get(Calendar.MINUTE),
            false
        ).show()

    }

    private fun handleDateImageClick() {


        //open date picker
        DatePickerDialog(
            requireActivity(), dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            reminder.createdAt.get(Calendar.YEAR),
            reminder.createdAt.get(Calendar.MONTH),
            reminder.createdAt.get(Calendar.DAY_OF_MONTH)
        ).show()

    }


    private fun handleSaveButton() {

        if (binding.reminderEditText.text.isBlank()) {
            MyUtils.showCustomToast(requireContext(), R.string.text_empty)

            return
        }
       else if(viewModel.mReminder.createdAt.timeInMillis <= Calendar.getInstance().timeInMillis){
      MyUtils.showCustomToast(requireContext(),R.string.old_date_error)

       return
   }

        disposable.add(viewModel.saveReminder(reminder)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { reminderId ->
                    //this reminder could be an update for existing reminder so we cancel any ongoing alarms
                    MyUtils.cancelAlarmManager(reminderId, context)
                    // set alarm manager
                    MyUtils.addAlarmManager(
                        reminderId,
                        context,
                        reminder.createdAt.timeInMillis,
                        reminder.repeat
                    )

                    requireActivity().onBackPressed()
                },
                {   //error
                        error ->
                    MyUtils.showCustomToast(requireContext(), R.string.error_saving_reminder)

                }
            ))


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            SPEECH_TO_TEXT_CODE -> {
                if (data == null) {
                    MyUtils.showCustomToast(requireContext(),R.string.something_went_wrong)
                    return
                }
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (result[0] != null) confirmText(result[0])
            }
            SELECT_PHONE_NUMBER -> {
                val contactUri = data?.data ?: return
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val cursor = requireActivity().contentResolver.query(
                    contactUri, projection,
                    null, null, null
                )

                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)

                    // do something with name and phone
                    if (binding.reminderEditText.text.isNotBlank())
                        binding.reminderEditText.append("\n")
                    binding.reminderEditText.append(
                        resources.getString(
                            R.string.phone_string,
                            name,
                            number
                        )
                    )
                    binding.reminderEditText.append("\n")
                    Linkify.addLinks(binding.reminderEditText, Linkify.PHONE_NUMBERS)
                } else {
                    MyUtils.showCustomToast(requireContext(), R.string.fetch_contact_failure)

                }
                cursor?.close()
            }
        }


    }


    private fun confirmText(convertedText: String) {

        //remove focus from reminder textview
        binding.reminderEditText.clearFocus()

        val dialog = MaterialDialog(requireContext()).show {
            customView(R.layout.speech_to_text_dialog)
            positiveButton(R.string.confirm) {
                //will execute on confirm press
                binding.reminderEditText.append(convertedText)
            }
            negativeButton(R.string.retry) {
                openSpeechToTextDialog()
            }
            title(0, getString(R.string.speech_confirmation))
        }


        dialog.getCustomView().findViewById<TextView>(R.id.textView).apply {
            append(convertedText)
        }

    }


    private fun updateReminderTime(hour: Int, minute: Int) {
        reminder.createdAt.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        reminderObserver.onNext(reminder)
    }


    private fun updateReminderDate(
        year: Int,
        month: Int,
        day: Int
    ) {
        reminder.createdAt.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        reminderObserver.onNext(reminder)

    }




    override fun onStop() {
        super.onStop()
        disposable.clear()

        /**this will be invoked to un-lock drawer only if parent of this fragment is main*/
        (requireActivity() as? ICommunication)?.setDrawerEnabled(true)


        MyUtils.hideKeyboard(requireActivity(), binding.saveFab)

    }


    override fun onStart() {
        super.onStart()
        /**this will be invoked to lock drawer only if parent of this fragment is main*/
        (requireActivity() as? ICommunication)?.setDrawerEnabled(false)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dateImage, R.id.dateText -> {
                handleDateImageClick()
            }
            R.id.timeImage, R.id.timeText -> {
                handleTimeImageClick()
            }
            R.id.repeatImage, R.id.repeatText -> {
                handleRepeatImageClick()
            }
            R.id.priorityImage, R.id.priorityText -> {
                handlePriorityImageClick()
            }
            R.id.reminderTypeImage, R.id.reminderTypeText -> {
                handleReminderTypeImageClick()
            }
            R.id.micImage -> {
                openSpeechToTextDialog()
            }
            R.id.contactsImage -> {
                pickNumberFromContacts()
            }
            R.id.backButton -> {
                requireActivity().onBackPressed()
            }
            R.id.saveFab -> {
                handleSaveButton()
            }
            R.id.keyboardImage -> {
                changeKeyboardVisibility()
            }
            else -> {
            }

        }
    }

    /**show features hints for first use*/
    private fun showHints() {
        if (MyUtils.getInt(requireContext(), FIRST_TIME_ADD_REMINDER) == 0) {

            val targets = TapTargetSequence(requireActivity())
                .targets(
                    TapTarget.forView(dateImage,   getString(R.string.options_buttons),
                        getString(R.string.click_to_edit_reminder))   .tintTarget(false)
                        .transparentTarget(false)
                        .cancelable(true),
                    TapTarget.forView(saveFab,   getString(R.string.save_button),
                        getString(R.string.save_button_to_save))   .tintTarget(false)
                        .transparentTarget(false)
                        .cancelable(true)
                )
            targets.start()






            MyUtils.putInt(requireContext(), FIRST_TIME_ADD_REMINDER,1)
        }
    }

}
