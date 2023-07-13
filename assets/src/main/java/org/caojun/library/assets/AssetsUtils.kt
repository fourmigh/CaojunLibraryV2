package org.caojun.library.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object AssetsUtils {

    fun readByteArray(context: Context, fileName: String): ByteArray? {
        return try {
            val assetMgr = context.assets
            val inputStream = assetMgr.open(fileName)
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            inputStream.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    fun readListString(context: Context, fileName: String): List<String> {
        val list = ArrayList<String>()
        try {
            val inputReader = InputStreamReader(context.resources.assets.open(fileName))
            val bufReader = BufferedReader(inputReader)
            var line = ""
            while (bufReader.readLine().also { line = it } != null) {
                if (!TextUtils.isEmpty(line)) {
                    list.add(line)
                }
            }
        } catch (e: Exception) {
        }
        return list
    }

    fun loadBitmap(context: Context, fileName: String): Bitmap? {
        try {
            val inputStream = context.resources.assets.open(fileName)
            val image = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            return image
        } catch (e: Exception) {
            Log.e("AssetsUtils", e.stackTraceToString())
        }
        return null
    }
}