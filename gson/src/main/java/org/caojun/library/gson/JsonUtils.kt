package org.caojun.library.gson

import com.google.gson.Gson
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.Collections.emptyList

object JsonUtils {

    private val gson = Gson()

    @JvmStatic
    fun <T> fromJson(json: String?, classOfT: Class<T>): T? {
        return try {
            gson.fromJson(json, classOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> fromJson(json: String?, typeOfT: Type): T? {
        return try {
            gson.fromJson(json, typeOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun toJson(data: Any?): String {
        return try {
            gson.toJson(data)
        } catch (e: Exception){
            ""
        }
    }

    fun <T> jsonToList(json: String?, cls: Class<T>): List<T> {
        val jsonArray = fromJson(json, ArrayList::class.java) ?: return emptyList()
        val list: MutableList<T> = ArrayList()
        for (json in jsonArray) {
            val map = json as Map<String,String>
            val data = fromJson(JSONObject(map).toString(), cls) ?: continue
            list.add(data)
        }
        return list
    }

    fun format(data: Any?, all: Boolean = false): String {
        return format(toJson(data), all)
    }

    fun format(json: String, all: Boolean = false): String {
        if (all) {
            return json.replace(",", ",\n")
        }
        return json.replace("},{", "},\n{")
    }
}