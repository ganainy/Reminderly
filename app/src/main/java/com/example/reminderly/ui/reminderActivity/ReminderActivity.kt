package com.example.reminderly.ui.reminderActivity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.example.reminderly.R
import com.example.reminderly.Utils.Utils
import com.example.reminderly.databinding.ActivityReminderBinding
import java.util.*


const val SPEECH_TO_TEXT_CODE = 1
const val SELECT_PHONE_NUMBER = 2

class ReminderActivity : AppCompatActivity() {

    private lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder
        )


        viewModel = ViewModelProvider(this).get(ReminderActivityViewModel::class.java)

        setupToolbar()

        initViewsDefaults()

        handleSaveButton()


    }

    private fun initViewsDefaults() {
        binding.dateText.text = com.example.reminderly.Utils.DateUtils.getCurrentDateFormatted()
        binding.timeText.text = com.example.reminderly.Utils.DateUtils.getCurrentTimeFormatted()

        binding.dateImage.setOnClickListener { handleDateImageClick() }
        binding.timeImage.setOnClickListener { handleTimeImageClick() }
        binding.repeatImage.setOnClickListener { handleRepeatImageClick() }
        binding.priorityImage.setOnClickListener { handlePriorityImageClick() }
        binding.notificationTypeImage.setOnClickListener { handleNotifyTypeImageClick() }
        binding.notifyInAdvanceImage.setOnClickListener { handleNotifyInAdvanceImageClick() }

        binding.micImage.setOnClickListener { openSpeechToTextDialog() }

        binding.contactsImage.setOnClickListener { pickNumberFromContacts() }
    }

    private fun pickNumberFromContacts() {
        val i = Intent(Intent.ACTION_PICK)
        i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(i, SELECT_PHONE_NUMBER)

    }


    private fun openSpeechToTextDialog() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, SPEECH_TO_TEXT_CODE)
        } else {
            Toast.makeText(this, getString(R.string.feature_not_supported), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleNotifyInAdvanceImageClick() {
        var num = 1
        var duration = "دقائق"
        val durationList = arrayOf(
            getString(R.string.minutes), getString(R.string.hours), getString(
                R.string.days
            ), getString(R.string.weeks)
        )

        val dialog = MaterialDialog(this).show {
            customView(R.layout.notify_in_advance_dialog)
            positiveButton(R.string.confirm) {
                //will execute on confirm press
                binding.notifyInAdvanceText.text =
                    "${Utils.convertToArabicNumber(num.toString())} $duration"
                viewModel.updateReminderNotifyAdvAmount(num)
                viewModel.updateReminderNotifyAdvUnit(duration)
            }
            negativeButton(R.string.cancel)
            title(0, getString(R.string.notification_in_advance_type))
        }

        //get value of numberPicker
        dialog.getCustomView().findViewById<NumberPicker>(R.id.numberPicker).apply {
            maxValue = 120
            minValue = 1
            setOnValueChangedListener { picker, oldVal, newVal ->
                num = newVal
            }
        }

        //get value of stringPicker
        dialog.getCustomView().findViewById<NumberPicker>(R.id.stringPicker).apply {
            minValue = 0
            maxValue = durationList.size - 1
            displayedValues = durationList
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { picker, oldVal, newVal ->
                duration = durationList[newVal]
            }
        }


    }


    private fun handleNotifyTypeImageClick() {
        MaterialDialog(this).show {
            listItemsSingleChoice(
                R.array.notify_type_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                binding.reminderTypeText.text = text
                viewModel.updateReminderNotificationType(index)
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.notification_type))

        }
    }

    private fun handlePriorityImageClick() {
        MaterialDialog(this).show {
            listItemsSingleChoice(
                R.array.priority_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                viewModel.updateReminderPriority(index)
                binding.priorityText.text = text
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.priority_type))

        }
    }

    private fun handleRepeatImageClick() {
        MaterialDialog(this).show {
            listItemsSingleChoice(
                R.array.repeat_items,
                initialSelection = 0
            ) { dialog, index, text ->
                // Invoked when the user selects an item
                binding.repeatText.text = text
                viewModel.updateReminderRepeat(index)
            }
            positiveButton(R.string.confirm)
            negativeButton(R.string.cancel)
            title(0, getString(R.string.repeat_count))
        }

    }

    private fun handleTimeImageClick() {
        val cal = Calendar.getInstance()

        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, min ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                //update time text view with selected date
                binding.timeText.text =
                    com.example.reminderly.Utils.DateUtils.formatTime(cal.time)
                viewModel.updateReminderTime(
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE)
                )
            }


        //open date picker
        TimePickerDialog(
            this@ReminderActivity, timeSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()

    }

    private fun handleDateImageClick() {
        val cal = Calendar.getInstance()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                //update date text view with selected date
                binding.dateText.text =
                    com.example.reminderly.Utils.DateUtils.formatDate(cal.time)
                viewModel.updateReminderDate(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH),
                    day = cal.get(Calendar.DAY_OF_MONTH)
                )

            }

        //open date picker
        DatePickerDialog(
            this@ReminderActivity, dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }


    private fun handleSaveButton() {
        binding.saveFab.setOnClickListener {
            if (binding.reminderEditText.text.isBlank()) {
                Toast.makeText(this, getString(R.string.text_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveReminder(binding.reminderEditText.text.toString())

        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        //add menu icon to toolbar (don't forget to override on option item selected for android.R.id.home to open drawer)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            SPEECH_TO_TEXT_CODE -> {
                if (data == null) {
                    Toast.makeText(
                        this,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.d("DebugTag", "onActivityResult: $result")
                if (result[0] != null) confirmText(result[0])
            }
            SELECT_PHONE_NUMBER -> {
                val contactUri = data?.data ?: return
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val cursor = contentResolver.query(
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
                    convertStringToClickable(name, number)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.fetch_contact_failure),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                cursor?.close()
            }
        }


    }

    private fun convertStringToClickable(name: String?, number: String?) {
        //make phone number clickable and add it to reminder text
        val text = "اتصل ب $name على الرقم $number"
        val ss = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openDialPadWithNumber(number)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
            }
        }
        ss.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        viewModel.updateReminderClickableString(text)

        if (binding.reminderEditText.text.isNotBlank()) binding.reminderEditText.append("\n")
        binding.reminderEditText.append(ss)
        binding.reminderEditText.append("\n")
        binding.reminderEditText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openDialPadWithNumber(phone: String?) {
        //copy phone to device dial when phone is clicked
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }


    private fun confirmText(convertedText: String) {

        //remove focus from reminder textview
        binding.reminderEditText.clearFocus()

        val dialog = MaterialDialog(this).show {
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
}

