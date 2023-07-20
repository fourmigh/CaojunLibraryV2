package org.caojun.library.pinyin

import com.github.promeg.pinyinhelper.Pinyin

object PinyinUtils {

    private fun getPinyin(pinyin: String, lowercase: Boolean): String {
        return if (lowercase) {
            pinyin.lowercase()
        } else {
            pinyin.uppercase()
        }
    }

    fun toPinyin(str: String, separator: String = "", lowercase: Boolean = true): String {
        val pinyin = Pinyin.toPinyin(str, separator)
        return getPinyin(pinyin, lowercase)
    }

    fun firstLetters(str: String, lowercase: Boolean = true): String {
        val sb = StringBuffer()
        for (c in str) {
            val py = Pinyin.toPinyin(c)
            sb.append(py[0])
        }
        val pinyin = sb.toString()
        return getPinyin(pinyin, lowercase)
    }


}