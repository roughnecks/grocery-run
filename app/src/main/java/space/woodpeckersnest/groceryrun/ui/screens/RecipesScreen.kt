package space.woodpeckersnest.groceryrun.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.data.model.Recipe
import space.woodpeckersnest.groceryrun.ui.components.AddFoodDialog
import space.woodpeckersnest.groceryrun.ui.components.RecipeNameDialog
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.RecipeViewModel

// "Recipes" tab: alphabetical list of recipe names, with optional
// ingredients management. Adding an ingredient checks the master list
// of foods; if not present, prompts the user to add it (with optional shelf & price).
@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val recipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val shelves by viewModel.allShelves.collectAsStateWithLifecycle()
    val selectedRecipeForIngredients by viewModel.selectedRecipeForIngredients.collectAsStateWithLifecycle()
    val pendingNewIngredientName by viewModel.pendingNewIngredientName.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var recipeToRename by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_recipe_title))
            }
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.recipes_empty),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = recipe.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.selectRecipeForIngredients(recipe) }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.manage_ingredients_action))
                        }
                        IconButton(onClick = { recipeToRename = recipe }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename_action))
                        }
                        IconButton(onClick = { viewModel.deleteRecipe(recipe) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_action))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        RecipeNameDialog(
            title = stringResource(R.string.add_recipe_title),
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addRecipe(name)
                showAddDialog = false
            }
        )
    }

    recipeToRename?.let { recipe ->
        RecipeNameDialog(
            title = stringResource(R.string.rename_recipe_title),
            initialName = recipe.name,
            onDismiss = { recipeToRename = null },
            onConfirm = { newName ->
                viewModel.renameRecipe(recipe, newName)
                recipeToRename = null
            }
        )
    }

    selectedRecipeForIngredients?.let { recipe ->
        RecipeIngredientsDialog(
            recipe = recipe,
            viewModel = viewModel,
            onDismiss = { viewModel.selectRecipeForIngredients(null) }
        )
    }

    pendingNewIngredientName?.let { newIngredientName ->
        selectedRecipeForIngredients?.let { recipe ->
            AddFoodDialog(
                shelves = shelves,
                initialName = newIngredientName,
                onDismiss = { viewModel.dismissNewIngredientDialog() },
                onConfirm = { name, shelfId, price ->
                    viewModel.confirmNewIngredientFood(recipe.id, name, shelfId, price)
                }
            )
        }
    }
}

@Composable
private fun RecipeIngredientsDialog(
    recipe: Recipe,
    viewModel: RecipeViewModel,
    onDismiss: () -> Unit
) {
    val ingredients by viewModel.currentRecipeIngredients.collectAsStateWithLifecycle()
    var ingredientInputName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ingredients_title, recipe.name)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ingredientInputName,
                        onValueChange = { ingredientInputName = it },
                        placeholder = { Text(stringResource(R.string.add_ingredient_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (ingredientInputName.isNotBlank()) {
                                viewModel.addIngredientByName(recipe.id, ingredientInputName)
                                ingredientInputName = ""
                            }
                        },
                        enabled = ingredientInputName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_action))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (ingredients.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_ingredients),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ingredients.forEach { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.removeIngredientFromRecipe(recipe.id, food.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_action))
                                }
                            }
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
