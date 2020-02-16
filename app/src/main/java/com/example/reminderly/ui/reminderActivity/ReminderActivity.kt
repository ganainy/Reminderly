package com.example.reminderly.ui.reminderActivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
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

const val SPEECH_TO_TEXT_CODE=1
class ReminderActivity : AppCompatActivity() {

    private lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding

    private val cal: Calendar by lazy { Calendar.getInstance() }

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

        handleDateImageClick()
        handleTimeImageClick()
        handleRepeatImageClick()
        handlePriorityImageClick()
        handleNotifyTypeImageClick()
        handleNotifyInAdvanceImageClick()

        binding.micImage.setOnClickListener {
            openSpeechToTextDialog()
        }

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
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleNotifyInAdvanceImageClick() {
        var num = 1
        var duration = "دقائق"
        val durationList = arrayOf("دقائق", "ساعات", "أيام", "اسابيع")

        binding.notifyInAdvanceImage.setOnClickListener {
            val dialog = MaterialDialog(this).show {
                customView(R.layout.notify_in_advance_dialog)
                positiveButton(R.string.confirm) {
                    //will execute on confirm press
                    binding.notifyInAdvanceText.text =
                        "${Utils.convertToArabicNumber(num.toString())} $duration"
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


    }


    private fun handleNotifyTypeImageClick() {
        binding.notificationTypeImage.setOnClickListener {
            MaterialDialog(this).show {
                listItemsSingleChoice(
                    R.array.notify_type_items,
                    initialSelection = 0
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    Log.d("DebugTag", "handleRepeatImageClick: $index $text")
                    binding.reminderTypeText.text = text
                }
                positiveButton(R.string.confirm)
                negativeButton(R.string.cancel)
                title(0, getString(R.string.notification_type))
            }
        }
    }

    private fun handlePriorityImageClick() {
        binding.priorityImage.setOnClickListener {
            MaterialDialog(this).show {
                listItemsSingleChoice(
                    R.array.priority_items,
                    initialSelection = 0
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    Log.d("DebugTag", "handleRepeatImageClick: $index $text")
                    binding.priorityText.text = text
                }
                positiveButton(R.string.confirm)
                negativeButton(R.string.cancel)
                title(0, getString(R.string.priority_type))
            }
        }
    }

    private fun handleRepeatImageClick() {
        binding.repeatImage.setOnClickListener {
            MaterialDialog(this).show {
                listItemsSingleChoice(
                    R.array.repeat_items,
                    initialSelection = 0
                ) { dialog, index, text ->
                    // Invoked when the user selects an item
                    Log.d("DebugTag", "handleRepeatImageClick: $index $text")
                    binding.repeatText.text = text
                }
                positiveButton(R.string.confirm)
                negativeButton(R.string.cancel)
                title(0, getString(R.string.repeat_count))
            }
        }
    }

    private fun handleTimeImageClick() {
        binding.timeImage.setOnClickListener {

            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _, hour, min ->
                    cal.set(Calendar.HOUR, hour)
                    cal.set(Calendar.MINUTE, min)
                    //update time text view with selected date
                    binding.timeText.text =
                        com.example.reminderly.Utils.DateUtils.formatTime(cal.time)
                }


            //open date picker
            TimePickerDialog(
                this@ReminderActivity, timeSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun handleDateImageClick() {
        binding.dateImage.setOnClickListener {

            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    //update date text view with selected date
                    binding.dateText.text =
                        com.example.reminderly.Utils.DateUtils.formatDate(cal.time)
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
    }


    private fun handleSaveButton() {
        binding.saveFab.setOnClickListener {
            //todo
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

        if (requestCode == SPEECH_TO_TEXT_CODE && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            Log.d("DebugTag", "onActivityResult: $result")
           if (result[0]!=null) confirmText(result[0])
        }

    }

    private fun confirmText(convertedText: String) {

        //remove focus from reminder textview
        binding.reminderTextView.clearFocus()

        val dialog = MaterialDialog(this).show {
            customView(R.layout.speech_to_text_dialog)
            positiveButton(R.string.confirm) {
                //will execute on confirm press
                binding.reminderTextView.setText(convertedText)
            }
            negativeButton(R.string.retry){
                openSpeechToTextDialog()
            }
            title(0, getString(R.string.speech_confirmation))
        }

        //get value of numberPicker
        dialog.getCustomView().findViewById<TextView>(R.id.textView).apply {
            text=convertedText
        }

    }
}

