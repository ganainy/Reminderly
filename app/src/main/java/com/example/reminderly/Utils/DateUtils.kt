package com.example.reminderly.Utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object{

            var instance= Date()


        fun getCurrentDateFormatted(): String {
            val locale = Locale("ar")
            val currentDate = instance
            val sdf = SimpleDateFormat("EEEE, dd MMMM", locale)
            return sdf.format(currentDate)

        }
    }



}