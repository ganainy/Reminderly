package com.example.reminderly.Utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*

class MyUtils {

    companion object {

        private val currentDate: Date
            get() {
                return Date()
            }


        /**-------------------------------------Date-------------------------------------*/

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



        /**-------------------------------------keyboard----------------------------------*/


        /**@param view :any view from the caller layout to get token from */
        fun hideKeyboard(context: Context, view: View) : Boolean? {

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            return imm?.hideSoftInputFromWindow(view.windowToken, 0)

        }

        fun showKeyboard(context: Context?){
            val imm =  context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

        }




        /**------------------------------Helpers------------------------------*/

        fun convertToArabicNumber(englishNum:String): String {
            val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            val builder = StringBuilder()
            for (i in englishNum.indices) {
                if (Character.isDigit(englishNum[i])) {
                    builder.append(arabicChars[englishNum[i].toInt() - 48])
                } else {
                    builder.append(englishNum[i])
                }
            }
            return builder.toString()
        }

    }


}