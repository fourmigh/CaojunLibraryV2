package org.caojun.library.room.sp

import android.text.TextUtils
import androidx.room.TypeConverter
import org.caojun.library.gson.JsonUtils

class ByteArrayListConverter {

    @TypeConverter
    fun arrayToString(list: ArrayList<ByteArray>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun stringToArray(json: String?): ArrayList<ByteArray> {
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        }
        return JsonUtils.fromJson(json!!, ArrayList<ByteArray>()::class.java) ?: return ArrayList()
    }
}