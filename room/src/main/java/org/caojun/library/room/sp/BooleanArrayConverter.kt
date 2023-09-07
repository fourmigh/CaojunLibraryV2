package org.caojun.library.room.sp

import android.text.TextUtils
import androidx.room.TypeConverter
import org.caojun.library.gson.JsonUtils

class BooleanArrayConverter {

    @TypeConverter
    fun arrayToString(list: ArrayList<Boolean>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun stringToArray(json: String?): ArrayList<Boolean> {
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        }
        return JsonUtils.fromJson(json!!, ArrayList<Boolean>()::class.java) ?: return ArrayList()
    }
}