package com.woodpeckersnest.groceryrun.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.woodpeckersnest.groceryrun.R
import com.woodpeckersnest.groceryrun.data.CurrencyHelper
import com.woodpeckersnest.groceryrun.data.repository.UserPreferencesRepository

@Composable
fun CurrencySelectionDialog(
    userPreferencesRepository: UserPreferencesRepository,
    onDismiss: () -> Unit
) {
    val savedSymbol = userPreferencesRepository.rawSavedCurrency
    val defaultSymbol = UserPreferencesRepository.getDefaultCurrencyForLanguage()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.currency_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CurrencyHelper.supportedCurrencies.forEach { currency ->
                    val isSelected = (currency.symbol == savedSymbol)

                    val label = if (currency.symbol.isEmpty()) {
                        stringResource(R.string.currency_auto_default, defaultSymbol)
                    } else {
                        currency.displayName
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                userPreferencesRepository.setCurrencySymbol(currency.symbol)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                userPreferencesRepository.setCurrencySymbol(currency.symbol)
                                onDismiss()
                            }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_action))
            }
        }
    )
}
