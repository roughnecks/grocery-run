package com.woodpeckersnest.groceryrun.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.woodpeckersnest.groceryrun.R
import com.woodpeckersnest.groceryrun.data.model.Food
import com.woodpeckersnest.groceryrun.data.model.Shelf

// Lists every food item (from the master "All Items" list) with a
// checkbox reflecting whether it currently belongs to this shelf.
// Checking/unchecking calls back to assign or unassign the shelf.
@Composable
fun ShelfProductsDialog(
    shelf: Shelf,
    allFoods: List<Food>,
    onDismiss: () -> Unit,
    onToggleFood: (food: Food, assign: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shelf_products_title, shelf.name)) },
        text = {
            if (allFoods.isEmpty()) {
                Text(stringResource(R.string.all_items_empty))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(allFoods, key = { it.id }) { food ->
                        val assignedToThisShelf = food.shelfId == shelf.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = assignedToThisShelf,
                                    onValueChange = { checked -> onToggleFood(food, checked) },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = assignedToThisShelf,
                                onCheckedChange = null // Click is handled by the Row
                            )
                            Text(text = food.name, modifier = Modifier.padding(start = 8.dp))
                        }
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