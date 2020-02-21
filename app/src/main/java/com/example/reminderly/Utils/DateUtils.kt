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

        private val dateFormat = SimpleDateFormat("EEEE, dd MMMM", locale)
        private val timeFormat = SimpleDateFormat("hh:mm a", locale)


        fun getCurrentDateFormatted(): String {
            return dateFormat.format(currentDate)
        }

        fun getCurrentTimeFormatted(): String {
            return timeFormat.format(currentDate)
        }

        fun formatDate(date: Date): String {
            return dateFormat.format(date)
        }

        fun formatTime(date: Date): String {
            return timeFormat.format(date)
        }


        fun getDateFromCalendar(calendar: Calendar): String {
            return dateFormat.format(calendar.time)
        }

        fun getTimeFromCalendar(calendar: Calendar): String {
            return timeFormat.format(calendar.time)
        }


    }


}