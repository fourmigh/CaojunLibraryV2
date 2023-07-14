package org.caojun.library.currency

import android.text.TextUtils
import java.lang.StringBuilder

object ChineseNumberUtils {

    private const val DEFAULT_CAPITAL = true
    private const val DEFAULT_CURRENCY = true

    private val Dot = "點"
    private val dot = "点"
    private val ChineseNumbers = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
    private val chineseNumbers = arrayOf("〇", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    private val ChineseUnits4 = arrayOf("", "拾", "佰", "仟")
    private val chineseUnits4 = arrayOf("", "十", "百", "千")
    private val ChineseUnits = arrayOf("", "万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载", "极")
    private val chineseUnits = arrayOf("", "万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载", "极")
    private val ChineseCurrency = arrayOf("元", "角", "分", "整")

    fun numToParts(num: String, currency: Boolean = DEFAULT_CURRENCY): Pair<String, String> {
        //小数
        var decimal = ""
        //整数
        var integer = ""

        val number = StringBuilder(num)
        var indexDot = num.indexOf('.')
        if (indexDot == 0) {
            number.insert(0, "0")
        }
        indexDot = number.indexOf('.')
        if (indexDot < 0) {
            //没有小数
            integer = number.toString()
            decimal = ""
        } else {
            integer = number.substring(0, indexDot)
            decimal = number.substring(indexDot)
            decimal = decimal.replace(".", "")
            if (currency && decimal.length > 2) {
                decimal = decimal.substring(0, 2)
            }
        }
        return Pair(integer, decimal)
    }

    fun integerToChinese(integer: String, capital: Boolean = DEFAULT_CAPITAL): String {
        val sb = StringBuilder()
        var lastNum = 0
        val cn = if (capital) ChineseNumbers else chineseNumbers
        val units4 = if (capital) ChineseUnits4 else chineseUnits4
        val units = if (capital) ChineseUnits else chineseUnits
        for (i in integer.indices) {
            val num = try {
                integer[i].digitToInt()
            } catch (e: Exception) {
                return ""
            }
            if (lastNum == 0 && num == 0) {
                continue
            }

            //位数
            val indexDigits = integer.length - i - 1
            //个万亿兆京垓秭穰沟涧正载极
            val indexUnits = indexDigits / 4
            val units = try {
                units[indexUnits]
            } catch (e: Exception) {
                return ""
            }
            if (sb.isNotEmpty() && sb[sb.length - 1].toString() == units) {
                sb.deleteCharAt(sb.length - 1)
            }

            try {
                sb.append(cn[num])
            } catch (e: Exception) {
                return ""
            }
            if (num > 0) {
                //个十白千
                val indexUnits4 = indexDigits % 4
                try {
                    sb.append(units4[indexUnits4])
                } catch (e: Exception) {
                    return ""
                }
            }
            try {
                sb.append(units)
            } catch (e: Exception) {
                return ""
            }
            lastNum = num
        }
        if (sb.isNotEmpty() && sb[sb.length - 1].toString() == cn[0]) {
            //最后一位是0
            sb.deleteCharAt(sb.length - 1)
        }
        return sb.toString()
    }

    fun decimalToChinese(decimal: String, capital: Boolean = DEFAULT_CAPITAL, currency: Boolean = DEFAULT_CURRENCY): String {
        val sb = StringBuilder()
        if (TextUtils.isEmpty(decimal) && currency) {
            sb.append(ChineseCurrency[3])
        } else {
            val cn = if (capital) ChineseNumbers else chineseNumbers
            for (i in decimal.indices) {
                val num = try {
                    decimal[i].digitToInt()
                } catch (e: Exception) {
                    return ""
                }
                try {
                    sb.append(cn[num])
                } catch (e: Exception) {
                    return ""
                }
                if (currency) {
                    sb.append(ChineseCurrency[i + 1])
                }
            }
        }
        return sb.toString()
    }

    fun numToChinese(num: String, capital: Boolean = DEFAULT_CAPITAL, currency: Boolean = DEFAULT_CURRENCY): String {
        val pair = numToParts(num, currency)
        val integer = integerToChinese(pair.first, capital)
        val decimal = decimalToChinese(pair.second, capital, currency)
        val dot = if (currency) {
            ChineseCurrency[0]
        } else if (TextUtils.isEmpty(decimal)) {
            ""
        } else if (capital) {
            Dot
        } else {
            dot
        }
        return "$integer$dot$decimal"
    }
}