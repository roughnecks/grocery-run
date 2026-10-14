package com.woodpeckersnest.groceryrun.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.woodpeckersnest.groceryrun.R

data class SupportedLanguage(
    val code: String, // Empty string for system default
    val nameRes: Int? = null,
    val nativeName: String? = null
)

object LanguageHelper {
    val supportedLanguages = listOf(
        SupportedLanguage(code = "", nameRes = R.string.language_system_default),
        SupportedLanguage(code = "it", nativeName = "Italiano"),
        SupportedLanguage(code = "en", nativeName = "English"),
        SupportedLanguage(code = "de", nativeName = "Deutsch"),
        SupportedLanguage(code = "fr", nativeName = "Français"),
        SupportedLanguage(code = "es", nativeName = "Español")
    )

    fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return ""
        val tag = locales.toLanguageTags().split(",").firstOrNull() ?: ""
        val mainCode = tag.split("-").firstOrNull() ?: ""
        return supportedLanguages.firstOrNull { it.code.equals(mainCode, ignoreCase = true) }?.code ?: ""
    }

    fun setLanguage(languageCode: String) {
        val localeList = if (languageCode.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
