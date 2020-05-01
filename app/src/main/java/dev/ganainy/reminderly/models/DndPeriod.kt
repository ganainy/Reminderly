package dev.ganainy.reminderly.models

/**class to save the start/end of the don't disturb period*/
data class DndPeriod(
    var startMinute: Int = 0,
    var startHour: Int = 0,
    var endMinute: Int = 0,
    var endHour: Int = 0
)