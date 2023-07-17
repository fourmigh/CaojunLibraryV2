package org.caojun.library.currency

import android.content.Context
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

/**
 * currentName: 人民币-Chinese Yuan
 * currentCode: CNY
 * currentCRO: 中国
 * currentNumber: 156
 * currentChineseName: 人民币
 * currentEnglishName: Chinese Yuan
 *
 * code: GBP
 * displayName: 英镑
 * symbol: £
 * subtype: GBP
 * type: currency
 * number: 826
 * roundingIncrement: 0.0
 */
object Currency {

    private const val SEPARATOR = "-"

    /**
     * <string name="currency_code">货币代码</string>
     * <string name="currency_number">数字代码</string>
     * <string name="currency_name">货币名称</string>
     * <string name="currency_cro">国家/地区/组织</string>
     */

    fun getCurrentCode(context: Context): String {
        return context.getString(R.string.currency_code)
    }

    fun getCurrentNumber(context: Context): Int {
        return try {
            context.getString(R.string.currency_number).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    fun getCurrentName(context: Context): String {
        return context.getString(R.string.currency_name)
    }

    fun getCurrentCRO(context: Context): String {
        return context.getString(R.string.currency_cro)
    }

    fun getCurrentChineseName(context: Context): String {
        val name = getCurrentName(context)
        if (name.contains(SEPARATOR)) {
            val names = name.split(SEPARATOR)
            if (names.isEmpty()) {
                return name
            }
            return names[0]
        }
        return name
    }

    fun getCurrentEnglishName(context: Context): String {
        val name = getCurrentName(context)
        if (name.contains(SEPARATOR)) {
            val names = name.split(SEPARATOR)
            if (names.size != 2) {
                return name
            }
            return names[1]
        }
        return name
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    val numbers = arrayOf(124, 156, 250, 280, 380, 392, 410, 826, 840, 901)

    fun number2Locale(number: Int = 156): Locale {
        return when (number) {
            250 -> Locale.FRANCE
            280 -> Locale.GERMANY
            380 -> Locale.ITALY
            392 -> Locale.JAPAN
            410 -> Locale.KOREA
            156 -> Locale.SIMPLIFIED_CHINESE
            901 -> Locale.TRADITIONAL_CHINESE
            826 -> Locale.UK
            840 -> Locale.US
            124 -> Locale.CANADA
            else -> Locale.PRC
        }
    }

    private fun getCurrency(locale: Locale = Locale.getDefault()): android.icu.util.Currency? {
        return try {
            android.icu.util.Currency.getInstance(locale)
        } catch (e: Exception) {
            null
        }
    }

    fun code(locale: Locale = Locale.getDefault()): String {
        return getCurrency(locale)?.currencyCode ?: ""
    }
    fun code(number: Int = 156): String {
        val locale = number2Locale(number)
        return code(locale)
    }

    fun displayName(locale: Locale = Locale.getDefault()): String {
        return getCurrency(locale)?.displayName ?: ""
    }
    fun displayName(number: Int): String {
        return displayName(number2Locale(number))
    }

    fun symbol(locale: Locale = Locale.getDefault()): String {
        return getCurrency(locale)?.symbol ?: ""
    }
    fun symbol(number: Int): String {
        return symbol(number2Locale(number))
    }

    fun subtype(locale: Locale = Locale.getDefault()): String {
        return getCurrency(locale)?.subtype ?: ""
    }

    fun type(locale: Locale = Locale.getDefault()): String {
        return getCurrency(locale)?.type ?: ""
    }

    fun number(locale: Locale = Locale.getDefault()): Int {
        return getCurrency(locale)?.numericCode ?: -1
    }

    fun roundingIncrement(locale: Locale = Locale.getDefault()): Double {
        return getCurrency(locale)?.roundingIncrement ?: -1.0
    }

    /**
     * 分单位的金额转换成两位小数格式
     */
    fun formatAmount(amount: Int, locale: Locale): String {
        val formatter = DecimalFormat.getInstance(locale)
        var doubleAmount = amount.toDouble() / 100
        doubleAmount += 0.001
        val result = formatter.format(doubleAmount)
        return result.substring(0, result.length - 1)
    }
    fun formatAmount(amount: Int, number: Int): String {
        val locale = number2Locale(number)
        return formatAmount(amount, locale)
    }

    fun formatAmount(input: String, number: Int): String {
        val amount = yuan2fen(input)
        return formatAmount(amount, number)
    }

    /**
     * 元转换成分
     */
    fun yuan2fen(text: String): Int {
        val value = BigDecimal(text)
        val result = value.multiply(BigDecimal("100"))
        return result.toInt()
    }
}