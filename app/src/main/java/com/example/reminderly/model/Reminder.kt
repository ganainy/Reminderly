package com.example.reminderly.model

import java.util.*

data class Reminder(var id:Int=0,
                    var text:String="",
                    var clickableStrings:MutableList<String> = mutableListOf<String>(),
                    var createdAt:Calendar=Calendar.getInstance(),
                    var repeat: Repeat=Repeat.ONCE,
                    var priority: Priority=Priority.LOW,
                    var repeatType: RepeatType=RepeatType.NOTIFICATION,
                    var notifyAdvAmount: Int=0,
                    var notifyAdvUnit: NotifyAdvUnit=NotifyAdvUnit.MINUTE) {

fun resetToDefaults(){
    text=""
    clickableStrings=mutableListOf()
    createdAt=Calendar.getInstance()
    repeat=Repeat.ONCE
    priority=Priority.LOW
    repeatType=RepeatType.NOTIFICATION
    notifyAdvAmount=0
    notifyAdvUnit=NotifyAdvUnit.MINUTE
}

}

enum class Repeat{
    ONCE,HOURLY,DAILY,WEEKLY,MONTHLY,YEARLY
}

enum class Priority{
    LOW,MEDIUM,HIGH
}

enum class RepeatType{
    NOTIFICATION,ALARM
}

enum class NotifyAdvUnit{
    MINUTE,HOUR,DAY,WEEK
}

