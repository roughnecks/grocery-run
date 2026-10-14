package com.woodpeckersnest.groceryrun.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.woodpeckersnest.groceryrun.data.LanguageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("grocery_run_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_WEEK = "selected_week"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_AUTO_ADD_RECIPE_INGREDIENTS = "auto_add_recipe_ingredients"

        fun getDefaultCurrencyForLanguage(): String {
            val appLanguage = LanguageHelper.getCurrentLanguageCode()
            return if (appLanguage.equals("en", ignoreCase = true)) "$" else "€"
        }
    }

    var selectedWeek: Int
        get() = sharedPreferences.getInt(KEY_SELECTED_WEEK, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_SELECTED_WEEK, value) }

    val rawSavedCurrency: String
        get() = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "") ?: ""

    private val _currencySymbol = MutableStateFlow(getSavedOrDefaultCurrency())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private fun getSavedOrDefaultCurrency(): String {
        val saved = rawSavedCurrency
        if (saved.isNotBlank()) return saved
        return getDefaultCurrencyForLanguage()
    }

    fun setCurrencySymbol(symbol: String) {
        sharedPreferences.edit { putString(KEY_CURRENCY_SYMBOL, symbol) }
        _currencySymbol.value = if (symbol.isBlank()) getDefaultCurrencyForLanguage() else symbol
    }

    fun refreshDefaultCurrency() {
        if (rawSavedCurrency.isBlank()) {
            _currencySymbol.value = getDefaultCurrencyForLanguage()
        }
    }

    private val _autoAddRecipeIngredients = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_AUTO_ADD_RECIPE_INGREDIENTS, true)
    )
    val autoAddRecipeIngredients: StateFlow<Boolean> = _autoAddRecipeIngredients.asStateFlow()

    fun setAutoAddRecipeIngredients(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_AUTO_ADD_RECIPE_INGREDIENTS, enabled) }
        _autoAddRecipeIngredients.value = enabled
    }
}
