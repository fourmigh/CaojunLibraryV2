package org.caojun.library.room.sp

import android.text.TextUtils
import androidx.room.TypeConverter
import org.caojun.library.gson.JsonUtils

class StringArrayConverter {

    @TypeConverter
    fun arrayToString(list: ArrayList<String>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun stringToArray(json: String?): ArrayList<String> {
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        }
        return JsonUtils.fromJson(json!!, ArrayList<String>()::class.java) ?: return ArrayList()
    }
}