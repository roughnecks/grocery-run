package space.woodpeckersnest.groceryrun.data

data class SupportedCurrency(
    val symbol: String, // Empty string for Auto
    val displayName: String
)

object CurrencyHelper {
    val supportedCurrencies = listOf(
        SupportedCurrency(symbol = "", displayName = ""),
        SupportedCurrency(symbol = "€", displayName = "€ (EUR)"),
        SupportedCurrency(symbol = "$", displayName = "$ (USD)"),
        SupportedCurrency(symbol = "£", displayName = "£ (GBP)"),
        SupportedCurrency(symbol = "CHF", displayName = "CHF (Swiss Franc)"),
        SupportedCurrency(symbol = "¥", displayName = "¥ (JPY / CNY)")
    )
}
