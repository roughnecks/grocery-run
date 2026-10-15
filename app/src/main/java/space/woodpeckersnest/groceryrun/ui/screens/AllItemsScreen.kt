package space.woodpeckersnest.groceryrun.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.data.model.Food
import space.woodpeckersnest.groceryrun.ui.components.AddFoodDialog
import space.woodpeckersnest.groceryrun.ui.viewmodel.AllItemsDisplayMode
import space.woodpeckersnest.groceryrun.ui.viewmodel.FoodViewModel
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.ShelfViewModel

// "All Items" tab: master list of every food item. Checking an
// item adds it to the Shopping List tab (shares the same data,
// only the "inShoppingList" flag changes).
@Composable
fun AllItemsScreen(
    foodViewModel: FoodViewModel = viewModel(factory = LocalViewModelFactory.current),
    shelfViewModel: ShelfViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val foods by foodViewModel.allFoods.collectAsStateWithLifecycle()
    val shelves by shelfViewModel.allShelves.collectAsStateWithLifecycle()
    val displayMode by foodViewModel.displayMode.collectAsStateWithLifecycle()
    val currencySymbol by foodViewModel.currencySymbol.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Reset scroll position to top when display mode changes
    LaunchedEffect(displayMode) {
        listState.scrollToItem(0)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFoods = remember(foods, searchQuery) {
        if (searchQuery.isBlank()) {
            foods
        } else {
            foods.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Prepare grouped data if in grouped mode
    val groupedFoods = remember(filteredFoods, shelves, displayMode) {
        if (displayMode == AllItemsDisplayMode.ALPHABETICAL) {
            emptyMap()
        } else {
            val shelfNameById = shelves.associate { it.id to it.name }
            filteredFoods
                .groupBy { shelfNameById[it.shelfId] }
                .toSortedMap(compareBy({ it == null }, { it }))
        }
    }

    // Optimization: remember event handlers to avoid recreation during recomposition
    val onCheckedChange = remember(foodViewModel) {
        { food: Food, checked: Boolean -> foodViewModel.setInShoppingList(food, checked) }
    }
    val onDelete = remember(foodViewModel) {
        { food: Food -> foodViewModel.deleteFood(food) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item_title))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar at the top
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_action))
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (filteredFoods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) stringResource(R.string.all_items_empty) 
                               else stringResource(R.string.search_empty),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
                ) {
                    if (displayMode == AllItemsDisplayMode.ALPHABETICAL) {
                        items(
                            items = filteredFoods,
                            key = { it.id },
                            contentType = { "food_item" } // Help Compose reuse views of the same type
                        ) { food ->
                            FoodRow(
                                food = food,
                                currencySymbol = currencySymbol,
                                onCheckedChange = { checked -> onCheckedChange(food, checked) },
                                onPriceChange = { price -> foodViewModel.updatePrice(food, price) },
                                onIncrementQuantity = { foodViewModel.incrementQuantity(food) },
                                onDecrementQuantity = { foodViewModel.decrementQuantity(food) },
                                onDelete = { onDelete(food) }
                            )
                        }
                    } else {
                        groupedFoods.forEach { (shelfName, items) ->
                            item(key = "header_${shelfName ?: "none"}") {
                                ShelfHeader(name = shelfName ?: stringResource(R.string.no_shelf))
                            }
                            items(
                                items = items,
                                key = { it.id },
                                contentType = { "food_item" }
                            ) { food ->
                                FoodRow(
                                    food = food,
                                    currencySymbol = currencySymbol,
                                    onCheckedChange = { checked -> onCheckedChange(food, checked) },
                                    onPriceChange = { price -> foodViewModel.updatePrice(food, price) },
                                    onIncrementQuantity = { foodViewModel.incrementQuantity(food) },
                                    onDecrementQuantity = { foodViewModel.decrementQuantity(food) },
                                    onDelete = { onDelete(food) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            shelves = shelves,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, shelfId, price ->
                foodViewModel.addFood(name, shelfId, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ShelfHeader(name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun FoodRow(
    food: Food,
    currencySymbol: String,
    onCheckedChange: (Boolean) -> Unit,
    onPriceChange: (Double) -> Unit,
    onIncrementQuantity: () -> Unit,
    onDecrementQuantity: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = food.inShoppingList,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = food.inShoppingList,
            onCheckedChange = null // Click is handled by the Row
        )
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyLarge
            )
            // Small price indicator
            val priceText = if (food.inShoppingList && food.quantity > 1) {
                if (food.price > 0) {
                    stringResource(
                        R.string.item_quantity_price_format,
                        food.price * food.quantity,
                        currencySymbol,
                        food.quantity,
                        food.price
                    )
                } else {
                    stringResource(R.string.quantity_prefix, food.quantity)
                }
            } else {
                stringResource(R.string.price_format, food.price, currencySymbol)
            }
            Text(
                text = priceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        var showPriceEdit by remember { mutableStateOf(false) }
        if (showPriceEdit) {
            PriceEditDialog(
                initialPrice = food.price,
                onDismiss = { showPriceEdit = false },
                onConfirm = {
                    onPriceChange(it)
                    showPriceEdit = false
                }
            )
        }

        // Price tag icon to edit price
        IconButton(onClick = { showPriceEdit = true }) {
            Icon(Icons.Default.Sell, contentDescription = stringResource(R.string.edit_price_title))
        }

        // Quantity controls
        if (food.inShoppingList) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (food.quantity > 1) {
                    IconButton(onClick = onDecrementQuantity) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = stringResource(R.string.remove_unit_action)
                        )
                    }
                }
                Text(
                    text = "${food.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onIncrementQuantity) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_unit_action)
                    )
                }
            }
        } else {
            IconButton(onClick = onIncrementQuantity) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_to_cart_action)
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_action))
        }
    }
}

@Composable
private fun PriceEditDialog(
    initialPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceStr by remember { mutableStateOf(if (initialPrice > 0) initialPrice.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_price_title)) },
        text = {
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it.replace(',', '.') },
                label = { Text(stringResource(R.string.price_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(priceStr.toDoubleOrNull() ?: 0.0) }) {
                Text(stringResource(R.string.save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    )
}