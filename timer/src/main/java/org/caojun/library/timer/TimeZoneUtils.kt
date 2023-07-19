package org.caojun.library.timer

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

    fun getTimeZone(timeZone: String): Pair<String, String>? {
        val tz = timeZone.split(TIME_ZONE_SEPARATOR)
        var left = ""
        var right = ""
        if (tz.size == 2) {
            left = tz[0]
            right = tz[1]
        } else if (tz.size == 1) {
            left = ""
            right = tz[0]
        } else {
            return null
        }
        return Pair(left, right)
    }

    fun getTimeZones2(): Pair<List<String>, Hashtable<String, List<String>>> {
        val timeZones = getTimeZones()
        val first = ArrayList<String>()
        val second = Hashtable<String, ArrayList<String>>()
        for (timeZone in timeZones) {
            val pair = getTimeZone(timeZone) ?: continue
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
}