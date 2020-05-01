package dev.ganainy.reminderly.database.converters

import androidx.room.TypeConverter
import java.util.*


/**required for room since i cannot save list in room directly , need to convert it to string first
 * then after that return it back as list*/
class CalendarConverter {
    @TypeConverter
    fun millisToCalendar(millis: Long): Calendar? {
       val cal= Calendar.getInstance()
        cal.timeInMillis=millis
        return cal
    }

    @TypeConverter
    fun calendarToMillis(calendar: Calendar): Long? {
        return calendar.timeInMillis
    }
}