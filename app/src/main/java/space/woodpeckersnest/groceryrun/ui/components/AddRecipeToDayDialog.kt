package space.woodpeckersnest.groceryrun.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.data.model.Recipe

// Picker listing every existing recipe with checkboxes for multiple
// selection.
@Composable
fun AddRecipeToDayDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onConfirm: (List<Recipe>) -> Unit
) {
    val selectedRecipes = remember { mutableStateListOf<Recipe>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_recipe_to_day_title)) },
        text = {
            if (recipes.isEmpty()) {
                Text(stringResource(R.string.recipes_empty))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
                        val isSelected = selectedRecipes.contains(recipe)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = isSelected,
                                    onValueChange = { checked ->
                                        if (checked) {
                                            selectedRecipes.add(recipe)
                                        } else {
                                            selectedRecipes.remove(recipe)
                                        }
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // Handled by the Row
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(recipe.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedRecipes.toList()) },
                enabled = selectedRecipes.isNotEmpty()
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