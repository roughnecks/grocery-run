package com.woodpeckersnest.groceryrun.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.woodpeckersnest.groceryrun.R
import com.woodpeckersnest.groceryrun.data.model.Shelf

// Dialog used from the "All Items" tab to add a new food item,
// with an optional shelf assignment via a dropdown.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    shelves: List<Shelf>,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, shelfId: Long?, price: Double) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var priceStr by remember { mutableStateOf("") }
    var selectedShelf by remember { mutableStateOf<Shelf?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_item_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.item_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.replace(',', '.') },
                    label = { Text(stringResource(R.string.price_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedShelf?.name ?: stringResource(R.string.no_shelf),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.shelf_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.no_shelf)) },
                            onClick = {
                                selectedShelf = null
                                expanded = false
                            }
                        )
                        shelves.forEach { shelf ->
                            DropdownMenuItem(
                                text = { Text(shelf.name) },
                                onClick = {
                                    selectedShelf = shelf
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    onConfirm(name.trim(), selectedShelf?.id, price)
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.add_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    )
}