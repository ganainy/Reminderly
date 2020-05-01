package dev.ganainy.reminderly.ui.calendarActivity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/**class purpose is to show yellow background under current day*/
class TodayDecorator: DayViewDecorator {

    private val color = Color.parseColor("#228BC34A")
    private val highlightDrawable = ColorDrawable(color)


    private var date = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return  day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(highlightDrawable)
    }


}