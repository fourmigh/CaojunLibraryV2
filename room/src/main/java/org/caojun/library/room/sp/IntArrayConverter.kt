package org.caojun.library.room.sp

import android.text.TextUtils
import androidx.room.TypeConverter
import org.caojun.library.gson.JsonUtils

class IntArrayConverter {

    @TypeConverter
    fun arrayToString(list: ArrayList<Int>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun stringToArray(json: String?): ArrayList<Int> {
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        }
        return JsonUtils.fromJson(json!!, ArrayList<Int>()::class.java) ?: return ArrayList()
    }
}