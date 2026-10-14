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
import com.woodpeckersnest.groceryrun.data.LanguageHelper

@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit
) {
    val currentCode = LanguageHelper.getCurrentLanguageCode()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                LanguageHelper.supportedLanguages.forEach { language ->
                    val isSelected = (language.code == currentCode) ||
                        (currentCode.isEmpty() && language.code.isEmpty())

                    val displayName = language.nameRes?.let { stringResource(it) }
                        ?: language.nativeName
                        ?: language.code

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                LanguageHelper.setLanguage(language.code)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                LanguageHelper.setLanguage(language.code)
                                onDismiss()
                            }
                        )
                        Text(
                            text = displayName,
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
