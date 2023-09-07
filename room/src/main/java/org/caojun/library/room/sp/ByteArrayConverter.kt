package org.caojun.library.room.sp

import android.text.TextUtils
import androidx.room.TypeConverter
import org.caojun.library.gson.JsonUtils

class ByteArrayConverter {

    @TypeConverter
    fun arrayToString(list: ArrayList<Byte>): String {
        return JsonUtils.toJson(list)
    }

    @TypeConverter
    fun stringToArray(json: String?): ArrayList<Byte> {
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        }
        return JsonUtils.fromJson(json!!, ArrayList<Byte>()::class.java) ?: return ArrayList()
    }
}