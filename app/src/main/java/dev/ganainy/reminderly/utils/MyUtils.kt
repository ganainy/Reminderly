package dev.ganainy.reminderly.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import dev.ganainy.reminderly.R
import dev.ganainy.reminderly.broadcast_receivers.NewReminderReceiver
import dev.ganainy.reminderly.broadcast_receivers.PersistentNotificationReceiver
import dev.ganainy.reminderly.database.Reminder
import dev.ganainy.reminderly.services.AlarmService
import dev.ganainy.reminderly.ui.mainActivity.MainActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 *  DONE_ACTION_FOR_REPEATING_REMINDERS:Int
 *  this value changes based on user settings
 *  0-> just cancel this notification and it will work normally in next repeat (default)
 *  1-> end the whole reminder
 *  */

/**
 *  DONE_ACTION_FOR_REMINDERS:Int
 *  this value changes based on user settings
 *  0-> done reminder are saved and can be accessed through menu (default)
 *  1-> done reminders are deleted
 *  */

const val DONE_ACTION_FOR_REPEATING_REMINDERS ="doneActionForRepeatingReminders"
/* 0-> just cancel this notification and it will work normally in next repeat (default) ,
  1-> end the whole reminder*/

const val ALLOW_PERSISTENT_NOTIFICATION ="allowPersistent_notification"
/* 0-> allowed (default) , 1-> not allowed*/

const val DONE_ACTION_FOR_REMINDERS ="doneActionForReminders"
/*0-> done reminder are saved and can be accessed through menu (default) ,  1-> done reminders are deleted*/



private const val PERSISTENT_CHANNEL_ID = "primary_notification_channel"
const val PERSISTENT_NOTIFICATION_ID = 0

const val REMINDER="reminder"
const val DONT_DISTURB_START_HOURS="dontDisturbStartHours"
const val DONT_DISTURB_START_MINUTES="dontDisturbStartMinutes"
const val DONT_DISTURB_END_HOURS="dontDisturbEndHours"
const val DONT_DISTURB_END_MINUTES="dontDisturbEndMinutes"
const val DND_OPTION_ENABLED="dndOptionEnabled" /*0->disabled , 1 -> enabled */
const val NIGHT_MODE_ENABLED="nightModeEnabled" /*0->disabled , 1 -> enabled */
const val FIRST_TIME_USE="firstTimeUser" /*0->first app use , 1 -> app opened before */
const val SHOWN_DRAWER_GUIDE="shownDrawerGuide" /*0->we need to promote user to click the calendar button , 1 ->no need to show guide */
const val FIRST_TIME_ADD_REMINDER="firstTimeAddReminder" /*0-> first time user is adding reminders show hints , 1-> don't show hints */
const val AD_CLICK_PER_SESSION="adClickPerSession" /*count clicked ad to temporarily ban user after certain amount*/
class MyUtils {

    companion object {

        private val currentDate: Date
            get() {
                return Date()
            }



        //region date

        private  lateinit var locale:Locale

        private var dateFormat = SimpleDateFormat("EEEE, dd MMMM")
        private var timeFormat = SimpleDateFormat("hh:mm a")

        fun setLocale(localeAbbreviation:String){
            locale=Locale(localeAbbreviation)
            dateFormat = SimpleDateFormat("EEEE, dd MMMM", locale)
            timeFormat = SimpleDateFormat("hh:mm a", locale)
        }

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

        fun formatTime(hour: Int,minute: Int): String {
            val calendar=Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY,hour)
            calendar.set(Calendar.MINUTE,minute)
            return timeFormat.format(calendar.time)
        }

        fun getDateFromCalendar(calendar: Calendar): String {
            return dateFormat.format(calendar.time)
        }

        fun getTimeFromCalendar(calendar: Calendar): String {
            return timeFormat.format(calendar.time)
        }

        //convert list of Calendar to list of CalendarDay
        fun getCalendarDays(activeReminderList: MutableList<Reminder>): MutableList<CalendarDay> {
            val calendarDays = mutableListOf<CalendarDay>()
            for (reminder in activeReminderList) {
                calendarDays.add(
                    CalendarDay.from(
                        reminder.createdAt.get(Calendar.YEAR),
                        reminder.createdAt.get(Calendar.MONTH) + 1,
                        reminder.createdAt.get(Calendar.DAY_OF_MONTH)
                    )
                )
            }
            return calendarDays
        }

        fun calendarDayToCalendar(calendarDay: CalendarDay): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, calendarDay.year)
                set(Calendar.MONTH, calendarDay.month - 1)
                set(Calendar.DAY_OF_MONTH, calendarDay.day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        fun calendarToCalendarDay(calendar: Calendar): CalendarDay {
            return CalendarDay.from(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        //takes date and return the very last second of it
        fun getEndOfCalendarDay(calendar: Calendar): Calendar {
           val nextDayCalendar by lazy {  Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            }
            return nextDayCalendar
        }

        //takes date and return the very first second of it
        fun getStartOfCalendarDay(calendar: Calendar): Calendar {
            val startOfDayCalendar by lazy {  Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            }
            return startOfDayCalendar
        }

        //endregion

        //region keyboard


        /**@param view :any view from the caller layout to get token from */
        fun hideKeyboard(context: Context, view: View): Boolean? {

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            return imm?.hideSoftInputFromWindow(view.windowToken, 0)

        }

        fun showKeyboard(context: Context?) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
//endregion

        //region helpers

        fun convertToArabicNumber(englishNum: String): String {
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

        fun convertRepeat(context: Context, repeat: Int): String {
            val repeatArray = context.resources.getStringArray(R.array.repeat_items)
            return repeatArray[repeat]
            }


        fun convertPriority(context: Context,priority: Int): String {
            val repeatArray = context.resources.getStringArray(R.array.priority_items)
            return repeatArray[priority]
        }

        fun convertReminderType(context: Context,type: Int): String {
            val repeatArray = context.resources.getStringArray(R.array.notify_type_items)
            return repeatArray[type]
        }

         fun isAppFirstUse(context: Context):Boolean {
            return getInt(context,FIRST_TIME_USE) ==0
        }


        /**@param arrayName should be in form R.array.___
         * @return string from the given resources array
         * */
        fun getStringFromResourceArray(context: Context,arrayName:Int,itemIndex:Int):String{
            val array: Array<String> = context.resources.getStringArray(arrayName)
            return array[itemIndex]
        }



        fun showCustomToast(context: Context,stringId:Int,length:Int =Toast.LENGTH_SHORT) {
            Toast.makeText(
                context,
                context.getString(stringId),
                length
            )
                .show()
        }


        /**this method checks if a date is in range between two dates*/
        fun isDndPeriod(context: Context): Boolean {

            //first check if dnd option is enabled
            if (getInt(context, DND_OPTION_ENABLED) == 0) {
                //dnd option is disabled
                return false
            }

            val startMinute = getInt(context, DONT_DISTURB_START_MINUTES)
            val startHour = getInt(context, DONT_DISTURB_START_HOURS)
            val endHour = getInt(context, DONT_DISTURB_END_HOURS)
            val endMinute = getInt(context, DONT_DISTURB_END_MINUTES)

            //get current minute and hour and compare them with dnd period
            val currentTime = Calendar.getInstance()

            val dndStart = Calendar.getInstance()
            dndStart.set(Calendar.HOUR_OF_DAY, startHour)
            dndStart.set(Calendar.MINUTE, startMinute)

            val dndEnd = Calendar.getInstance()
            dndEnd.set(Calendar.HOUR_OF_DAY, endHour)
            dndEnd.set(Calendar.MINUTE, endMinute)

            return currentTime.after(dndStart) && currentTime.before(dndEnd)

        }


        /**cancel the reminder UI (notification) depending on if its alarm or notification reminder*/
         fun closeReminder(
            reminder: Reminder,
            context: Context
        ) {
            if (reminder.reminderType == 1) {
                //alarm reminder, we need to stop service to stop ringing and repeating notification
                stopAlarmService(context)
            }
            cancelNotification(reminder.id, context)
        }

            //endregion

        //region alarm manager

        /**setup alarm manager to trigger NewReminderReceiver on reminder date*/
        fun addAlarmManager(reminder:Reminder, context: Context?) {
            //add new onetime alarm OR repeat alarm depending on repeat value

            when (reminder.repeat) {
                0 -> {//one time reminder
                    addOneTimeAlarm(reminder, context)
                }
                1 -> { //every hour reminder
                    addPeriodicAlarm(reminder, context, 3600 * 1000)
                }
                2 -> { //every day reminder
                    addPeriodicAlarm(reminder, context, 3600 * 1000 * 24)
                }
                3 -> { //every week reminder
                    addPeriodicAlarm(reminder, context, 3600 * 1000 * 24 * 7)
                }
                4 -> { //every month reminder
                    addPeriodicAlarm(reminder, context,  3600 * 1000 * 24 * 30L)
                }
                5 -> { //every year reminder
                    addPeriodicAlarm(reminder, context,  3600 * 1000 * 24 * 365L)
                }
            }
        }


        private fun addOneTimeAlarm(
            reminder: Reminder,
            context: Context?
        ) {
            val notifyIntent = Intent(context, NewReminderReceiver::class.java)
            notifyIntent.putExtra(REMINDER, reminder.getStringFromReminder())
            val notifyPendingIntent = PendingIntent.getBroadcast(
                context, reminder.id.toInt(), notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.createdAt.timeInMillis, notifyPendingIntent)
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, reminder.createdAt.timeInMillis, notifyPendingIntent)
            }
        }


        private fun addPeriodicAlarm(
            reminder: Reminder,
            context: Context?,
            repeatMillis: Long
        ) {

            val notifyIntent = Intent(context, NewReminderReceiver::class.java)

            notifyIntent.putExtra(REMINDER, reminder.getStringFromReminder())

            val notifyPendingIntent = PendingIntent.getBroadcast(context, reminder.id.toInt(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, reminder.createdAt.timeInMillis, repeatMillis, notifyPendingIntent)

        }


        fun cancelAlarmManager(
            reminder: Reminder,
            context: Context?
        ) {
            val notifyIntent = Intent(context, NewReminderReceiver::class.java)
            notifyIntent.putExtra(REMINDER, reminder.getStringFromReminder())
            val notifyPendingIntent = PendingIntent.getBroadcast(
                context, reminder.id.toInt(), notifyIntent,
                PendingIntent.FLAG_ONE_SHOT
            )
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(notifyPendingIntent)

        }

         fun cancelNotification(reminderId: Long, context: Context?) {
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(reminderId.toInt())
        }

        //endregion

        //region general utils



        fun postponeReminder(
            reminder: Reminder,
            context: Context?,
            day: Int,
            hour: Int,
            minute: Int
        ): Reminder? {
            /** postpone reminder with passed duration*/

            reminder.createdAt.apply {
                add(Calendar.DAY_OF_MONTH, day)
                add(Calendar.HOUR_OF_DAY, hour)
                add(Calendar.MINUTE, minute)
            }


            /** will check that the new reminder date is bigger than current date and return null
             *  if not; because its useless to postpone reminder to a previous date*/

            return if (reminder.createdAt.before(Calendar.getInstance())) {
                if (context != null) {
                    showCustomToast(context,R.string.must_be_upcoming_date)
                }
                //remove added duration since reminder won't be updated
                reminder.createdAt.apply {
                    add(Calendar.DAY_OF_MONTH, -day)
                    add(Calendar.HOUR_OF_DAY, -hour)
                    add(Calendar.MINUTE, -minute)
                }
                null
            } else {
                reminder
            }


        }


        /**similar to pospone reminder but won't check if date of postponed reminder is valid*/
        fun forcePostponeReminder(
            reminder: Reminder,
            day: Int,
            hour: Int,
            minute: Int
        ): Reminder {
            /** postpone reminder with passed duration*/

            reminder.createdAt.apply {
                add(Calendar.DAY_OF_MONTH, day)
                add(Calendar.HOUR_OF_DAY, hour)
                add(Calendar.MINUTE, minute)
            }
             return reminder
        }

        /**stop the notification or any ongoing ringing alarm on showing postpone dialog*/
        fun stopAlarmService(context: Context) {
            val notifyIntent = Intent(context, AlarmService::class.java)
            context.stopService(notifyIntent)
        }

        //endregion

        //region shared preferences

        fun putString(context: Context,key:String,data:String){
            val pref: SharedPreferences =
                context.applicationContext
                    .getSharedPreferences("MyPref", 0)

            val editor = pref.edit()

            editor.putString(key, data)

            editor.apply()
        }

        fun getString(context: Context,key:String):String?{
            val pref: SharedPreferences =
                context.applicationContext
                    .getSharedPreferences("MyPref", 0)
           return pref.getString(key, null)
        }

        fun putInt(context: Context,key:String,data:Int){
            val pref: SharedPreferences =
                context.applicationContext
                    .getSharedPreferences("MyPref", 0)

            val editor = pref.edit()

            editor.putInt(key, data)

            editor.apply()
        }

        fun getInt(context: Context,key:String):Int{
            val pref: SharedPreferences =
                context.applicationContext
                    .getSharedPreferences("MyPref", 0)
            return pref.getInt(key, 0) //0 is default value which matches default value of settings
        }

        //endregion

        //region notification
        /**show persistent notification to allow user to add reminder if app is closed*/
        fun sendPersistentNotification(context: Context,todayReminders: MutableList<Reminder> ) {

             val mNotifyManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

            if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O
            ) {
                // Create a NotificationChannel
                val notificationChannel = NotificationChannel(
                    PERSISTENT_CHANNEL_ID,
                    "Reminder Persistent Notification", NotificationManager.IMPORTANCE_LOW
                )
                notificationChannel.description = "Notification from Reminderly"
                mNotifyManager.createNotificationChannel(notificationChannel)
            }

            val notificationBuilder = getPersistentNotificationBuilder(context,todayReminders)
            mNotifyManager.notify(PERSISTENT_NOTIFICATION_ID, notificationBuilder?.build())

        }



        private fun getPersistentNotificationBuilder(context: Context,todayReminders: MutableList<Reminder>): NotificationCompat.Builder? {
            val notificationButtonText = when (todayReminders.size) {
                0 -> context.resources.getString(R.string.add_reminders)
                else -> context.resources.getString(R.string.add_other_reminders)
            }

            val notificationText = when (todayReminders.size) {
                0 -> context.resources.getString(R.string.no_reminders_today)
                else -> {
                    var pastReminderOfToday = 0
                    var upcomingReminderOfToday = 0
                    for (reminder in todayReminders) {
                        if (reminder.createdAt.before(Calendar.getInstance())) {
                            pastReminderOfToday++
                        } else {
                            upcomingReminderOfToday++
                        }
                    }
                    context.resources.getString(
                        R.string.reminders_today,
                        todayReminders.size,
                        pastReminderOfToday,
                        upcomingReminderOfToday
                    )
                }
            }





            /**new reminder pending intent to pass to notification builder action*/
            val newReminderIntent = Intent(context, MainActivity::class.java)
            newReminderIntent.putExtra("newReminder", "")
            newReminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val newReminderPendingIntent = PendingIntent.getActivity(
                context,
                0, newReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            /**open app pending intent to pass to notification builder contentIntent*/
            val contentIntent = Intent(context, MainActivity::class.java)
            contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )


            /**action to disable persistent notification*/
            val disablePersistentNotificationIntent = Intent(context, PersistentNotificationReceiver::class.java)
            val disablePersistentNotificationPendingIntent = PendingIntent.getBroadcast(
                context,
                2, disablePersistentNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            return NotificationCompat.Builder(context, PERSISTENT_CHANNEL_ID)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_bell_white)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setWhen(0)
                .addAction(
                    R.drawable.ic_add_white,
                    notificationButtonText,
                    newReminderPendingIntent
                )
                .addAction(
                    R.drawable.ic_delete_grey,
                    context.getString(R.string.remove_this_persistent_notif),
                    disablePersistentNotificationPendingIntent
                )
        }


        fun cancelPersistentNotification(context: Context) {
            val mNotifyManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyManager.cancel(PERSISTENT_NOTIFICATION_ID)
        }

        //endregion

        //region gson

        var gson: Gson?=null

        fun getJson(): Gson {

            if (gson==null){
                gson= Gson()
            }
            return gson as Gson
        }


        fun Reminder.getStringFromReminder():String{
            return getJson().toJson(this)
        }

        fun String.getReminderFromString():Reminder{
            return getJson().fromJson(this,Reminder::class.java)
        }
    }

        //endregion

    }





