package org.caojun.library.timer

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs

object TimeUtils {

    enum class WeekDay {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        XINGQIBA
    }

    private val LocalTimeZone = TimeZone.getDefault()
    const val DEFAULT_DATA_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

    @SuppressLint("SimpleDateFormat")
    private fun getSimpleDateFormat(dateFormat: String = DEFAULT_DATA_FORMAT, timeZone: TimeZone = LocalTimeZone): SimpleDateFormat {
        val df = SimpleDateFormat()
        df.timeZone = timeZone
        df.applyLocalizedPattern(dateFormat)
        return df
    }

    @SuppressLint("SimpleDateFormat")
    private fun getSimpleDateFormat(dateFormat: String = DEFAULT_DATA_FORMAT, timeZone: String = LocalTimeZone.id): SimpleDateFormat {
        val df = SimpleDateFormat(dateFormat)
        df.timeZone = TimeZone.getTimeZone(timeZone)
        return df
    }

    fun getTime(timeZone: TimeZone = LocalTimeZone, dateFormat: String = DEFAULT_DATA_FORMAT, time: Long = System.currentTimeMillis()): String {
        if (TextUtils.isEmpty(dateFormat)) {
            return ""
        }
        val df = getSimpleDateFormat(dateFormat, timeZone)
        val date = Date(abs(time))
        return df.format(date)
    }

    fun getTime(timeZone: String = LocalTimeZone.id, dateFormat: String = DEFAULT_DATA_FORMAT, time: Long = System.currentTimeMillis()): String {
        return getTime(TimeZone.getTimeZone(timeZone), dateFormat, time)
    }

    fun getTime(dateFormat: String = DEFAULT_DATA_FORMAT): String {
        return getTime(LocalTimeZone, dateFormat, System.currentTimeMillis())
    }

    fun getTime(): String {
        return getTime(LocalTimeZone, DEFAULT_DATA_FORMAT, System.currentTimeMillis())
    }

    fun getWeekDay(timestamp: Long = System.currentTimeMillis()): WeekDay {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val weekDays = WeekDay.values()
        for (wd in weekDays) {
            if (wd.ordinal == weekDay) {
                return wd
            }
        }
        return WeekDay.XINGQIBA
    }
}

//TextView简易添加日志
fun TextView.addLog(log: String) {
    val time = TimeUtils.getTime()
    val msg = "[$time] $log"
    val text = this.text.toString()
    setText("$msg\n$text")
}