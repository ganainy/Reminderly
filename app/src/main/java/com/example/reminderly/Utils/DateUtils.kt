package com.example.reminderly.Utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {

        private val currentDate: Date
            get() {
                return Date()
            }

        private val locale = Locale("ar")


        fun getCurrentDateFormatted(): String {
            val sdf = SimpleDateFormat("EEEE, dd MMMM", locale)
            return sdf.format(currentDate)

        }

        fun getCurrentTimeFormatted(): String {
            val sdf = SimpleDateFormat("hh:mm a", locale)
            return sdf.format(currentDate)
        }

        fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("EEEE, dd MMMM", locale)
            return sdf.format(date)
        }

        fun formatTime(date: Date): String {
            val sdf = SimpleDateFormat("hh:mm a", locale)
            return sdf.format(date)
        }


    }


}