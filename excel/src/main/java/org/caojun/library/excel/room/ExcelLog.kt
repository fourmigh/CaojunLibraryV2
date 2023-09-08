package org.caojun.library.excel.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.caojun.library.timer.TimeUtils

@Entity
class ExcelLog {

    @PrimaryKey(autoGenerate = true)
    var id = 0L

    var millis = System.currentTimeMillis()
    var type = LogType.V
    var tag = ""
    var className = ""
    var lineNumber = 0
    var methodName = ""
    var log = ""

    fun getDate(): String {
        return TimeUtils.getTime("yyyy-MM-dd", millis)
    }

    fun getTime(): String {
        return TimeUtils.getTime("HH:mm:ss.SSS", millis)
    }

    fun getPosition(): String {
        return "($className:$lineNumber)#$methodName"
    }

    fun getCells(): List<String> {
        val array = arrayOf(getDate(), getTime(), type.name, tag, getPosition(), log)
        return array.toList()
    }
}