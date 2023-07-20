package org.caojun.library.resources

import android.content.Context
import java.util.Locale

object LocaleUtils {

    fun getLocales(): Array<Locale> {
        return Locale.getAvailableLocales()
    }

    fun setLocale(context: Context, locale: Locale) {
        val res = context.resources
        val config = res.configuration
        config.locale = locale
        res.updateConfiguration(config, res.displayMetrics);
    }
}