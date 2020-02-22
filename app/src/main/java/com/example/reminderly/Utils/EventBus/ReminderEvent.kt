package com.example.reminderly.Utils.EventBus

import com.example.reminderly.database.Reminder

data class ReminderEvent(val overdueReminders:MutableList<Reminder> ,
                         val todayReminders:MutableList<Reminder> ,
                         val upcomingReminders:MutableList<Reminder> )