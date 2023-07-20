package org.caojun.library.timer

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs

object TimeUtils {

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

    fun getTime(): String {
        return getTime(LocalTimeZone, DEFAULT_DATA_FORMAT, System.currentTimeMillis())
    }
}

//TextView简易添加日志
fun TextView.addLog(log: String) {
    val time = TimeUtils.getTime()
    val msg = "[$time] $log"
    val text = this.text.toString()
    setText("$msg\n$text")
}