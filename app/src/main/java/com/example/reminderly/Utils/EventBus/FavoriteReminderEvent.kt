package com.example.reminderly.Utils.EventBus

import com.example.reminderly.database.Reminder

data class FavoriteReminderEvent(val favoriteReminders:MutableList<Reminder>)
