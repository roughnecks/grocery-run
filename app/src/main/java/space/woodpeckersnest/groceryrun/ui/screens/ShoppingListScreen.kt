package space.woodpeckersnest.groceryrun.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import space.woodpeckersnest.groceryrun.data.model.Food
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.ShoppingListViewModel

// "Shopping List" tab: only checked items, grouped by shelf in
// alphabetical order. Tapping the check icon removes an item
// from the view after it has been purchased.
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val groups by viewModel.groupedShoppingList.collectAsStateWithLifecycle()
    val totalCost by viewModel.totalCost.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (groups.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.shopping_total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.price_format, totalCost, currencySymbol),
                            style = MaterialTheme.typography.titleMedium, // Reduced size from titleLarge
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.shopping_list_empty),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            // Optimization: remember the event handler
            val onPurchased = remember(viewModel) {
                { food: Food -> viewModel.markPurchased(food) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groups.forEach { group ->
                    item(
                        key = "header_${group.shelfName ?: "none"}",
                        contentType = "header"
                    ) {
                        ShelfHeader(name = group.shelfName ?: stringResource(R.string.no_shelf))
                    }
                    items(
                        items = group.items,
                        key = { it.id },
                        contentType = { "shopping_item" }
                    ) { food ->
                        ShoppingListRow(
                            food = food,
                            currencySymbol = currencySymbol,
                            onPurchased = { onPurchased(food) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelfHeader(name: String) {
    // Shelf name colored with primary color, enclosed in a 90% width rounded surface
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
private fun ShoppingListRow(food: Food, currencySymbol: String, onPurchased: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val displayName = if (food.quantity > 1) {
                "${food.quantity}x ${food.name}"
            } else {
                food.name
            }
            Text(text = displayName, style = MaterialTheme.typography.bodyLarge)

            val priceText = if (food.quantity > 1) {
                if (food.price > 0) {
                    stringResource(
                        R.string.item_quantity_price_format,
                        food.price * food.quantity,
                        currencySymbol,
                        food.quantity,
                        food.price
                    )
                } else null
            } else {
                if (food.price > 0) {
                    stringResource(R.string.price_format, food.price, currencySymbol)
                } else null
            }

            if (priceText != null) {
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        IconButton(onClick = onPurchased) {
            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.mark_purchased_action))
        }
    }
}