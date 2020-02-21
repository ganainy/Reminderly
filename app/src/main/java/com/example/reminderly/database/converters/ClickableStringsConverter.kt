package com.example.reminderly.database.converters

import androidx.room.TypeConverter


/**required for room since i cannot save list in room directly , need to convert it to string first
 * then after that return it back as list*/
class ClickableStringsConverter {
    @TypeConverter
    fun stringToList(value: String): MutableList<String> {
        return value.split("\\s*,\\s*").toMutableList()
    }

    @TypeConverter
    fun listToString(clickableStrings: MutableList<String>): String? {
        var value= StringBuilder()
        for (clickableString  in clickableStrings){
            value.append(clickableString)
            value.append(",")
        }
        return value.toString()
    }
}