package org.caojun.library.timer

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Enumeration
import java.util.Hashtable
import java.util.TimeZone

object TimeZoneUtils {

    private const val TIME_ZONE_SEPARATOR = "/"

    fun getTimeZones(): Array<String> {
        return TimeZone.getAvailableIDs()
    }

    fun getTimeZone(leftIndex: Int, rightIndex: Int): String {
        val timeZones = getTimeZones2()
        val leftText = timeZones.first[leftIndex]
        val rightText = timeZones.second[leftText]!![rightIndex]
        return getTimeZone(leftText, rightText)
    }
    fun getTimeZone(leftText: String, rightText: String): String {
        val sb = StringBuilder()
        sb.append(leftText)
        if (sb.isNotEmpty()) {
            sb.append(TIME_ZONE_SEPARATOR)
        }
        sb.append(rightText)
        return sb.toString()
    }

    private fun getTimeZonePair(timeZone: String): Pair<String, String>? {
        val tz = timeZone.split(TIME_ZONE_SEPARATOR)
        return if (tz.size == 2) {
            Pair(tz[0], tz[1])
        } else if (tz.size == 1) {
            Pair("", tz[0])
        } else {
            null
        }
    }

    fun getTimeZones2(): Pair<List<String>, Hashtable<String, List<String>>> {
        val timeZones = getTimeZones()
        val first = ArrayList<String>()
        val second = Hashtable<String, ArrayList<String>>()
        for (timeZone in timeZones) {
            val pair = getTimeZonePair(timeZone) ?: continue
            val left = pair.first
            val right = pair.second
            if (!first.contains(left)) {
                first.add(left)
            }
            if (second.containsKey(left)) {
                val list = second[left]
                list?.add(right)
            } else {
                val list = ArrayList<String>()
                list.add(right)
                second[left] = list
            }
        }
        val third = Hashtable<String, List<String>>()
        val keys: Enumeration<String> = second.keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val value = second[key]
            third[key] = value
        }
        return Pair(first, third)
    }

    fun getTimeZone(timeZone: String): TimeZone {
        return TimeZone.getTimeZone(timeZone)
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormatTime(timeZone: String, dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS"): String {
        val format = SimpleDateFormat(dateFormat)
        format.timeZone = getTimeZone(timeZone)
        return format.format(Date())
    }
}