package dev.ganainy.reminderly.miscellaneous

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.utils.MyUtils
import java.util.*


@BindingAdapter("setDateFromCalendar")
fun setDateFromCalendar(textView: TextView, calendar: Calendar) {
    textView.text = MyUtils.getDateFromCalendar(calendar)
}

@BindingAdapter("setTimeFromCalendar")
fun setTimeFromCalendar(textView: TextView, calendar: Calendar) {
    textView.text = MyUtils.getTimeFromCalendar(calendar)
}

@BindingAdapter("setRepeatType")
fun setRepeatType(textView: TextView, repeat: Int) {
    textView.text = when (repeat) {
        0 -> textView.context.getString(R.string.once)
        1 -> textView.context.getString(R.string.every_hour)
        2 -> textView.context.getString(R.string.daily)
        3 -> textView.context.getString(R.string.weekly)
        4 -> textView.context.getString(R.string.monthly)
        5 -> textView.context.getString(R.string.yearly)
        else -> throw Exception("unknown type")
    }
}

@BindingAdapter("setFavorite")
fun setFavorite(imageView: ImageView, favorite: Boolean) {
    if (favorite)
    imageView.setImageDrawable(imageView.resources.getDrawable(R.drawable.ic_star_yellow,null))
    else
        imageView.setImageDrawable(imageView.resources.getDrawable(R.drawable.ic_star_grey,null))
}


@BindingAdapter("setPriority")
fun setPriority(imageView: ImageView, priority: Int) {
   imageView.setImageDrawable(when(priority){
       0->imageView.resources.getDrawable(R.drawable.lighter_green_round_bg,null)
       1->imageView.resources.getDrawable(R.drawable.yellow_round_bg,null)
       2->imageView.resources.getDrawable(R.drawable.red_round_bg,null)
       else -> throw Exception("unknown Priority")
   })
}

@BindingAdapter("setHeaderText")
fun setHeaderText(textView: TextView, header: Int) {
    textView.text = when (header) {
        1 -> textView.context.getString(R.string.overdue)
        2 -> textView.context.getString(R.string.today)
        3 -> textView.context.getString(R.string.upcoming)
        else -> throw Exception("unknown type")
    }
}




