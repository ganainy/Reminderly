package com.example.reminderly.Utils

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setCurrentDate")
fun setCurrentDate(textView: TextView,uselessParam:Int?) {
textView.text=DateUtils.getCurrentDateFormatted()
}