package com.woodpeckersnest.groceryrun.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woodpeckersnest.groceryrun.R
import com.woodpeckersnest.groceryrun.data.model.Shelf
import com.woodpeckersnest.groceryrun.ui.components.ShelfNameDialog
import com.woodpeckersnest.groceryrun.ui.components.ShelfProductsDialog
import com.woodpeckersnest.groceryrun.ui.viewmodel.FoodViewModel
import com.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import com.woodpeckersnest.groceryrun.ui.viewmodel.ShelfViewModel

// "Shelves" tab: create/rename/delete shelves (categories), and
// assign/unassign master-list food items to each shelf. Shelves
// are always shown alphabetically, matching how they're used to
// group the shopping list.
@Composable
fun ShelvesScreen(
    shelfViewModel: ShelfViewModel = viewModel(factory = LocalViewModelFactory.current),
    foodViewModel: FoodViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val shelves by shelfViewModel.allShelves.collectAsStateWithLifecycle()
    val allFoods by foodViewModel.allFoods.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var shelfToRename by remember { mutableStateOf<Shelf?>(null) }
    var shelfToManageProducts by remember { mutableStateOf<Shelf?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_shelf_title))
            }
        }
    ) { padding ->
        if (shelves.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.shelves_empty),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
            ) {
                items(shelves, key = { it.id }) { shelf ->
                    ShelfRow(
                        shelf = shelf,
                        onManageProducts = { shelfToManageProducts = shelf },
                        onRename = { shelfToRename = shelf },
                        onDelete = { shelfViewModel.deleteShelf(shelf) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ShelfNameDialog(
            title = stringResource(R.string.add_shelf_title),
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                shelfViewModel.addShelf(name)
                showAddDialog = false
            }
        )
    }

    shelfToRename?.let { shelf ->
        ShelfNameDialog(
            title = stringResource(R.string.rename_shelf_title),
            initialName = shelf.name,
            onDismiss = { shelfToRename = null },
            onConfirm = { newName ->
                shelfViewModel.renameShelf(shelf, newName)
                shelfToRename = null
            }
        )
    }

    shelfToManageProducts?.let { shelf ->
        ShelfProductsDialog(
            shelf = shelf,
            allFoods = allFoods,
            onDismiss = { shelfToManageProducts = null },
            onToggleFood = { food, assign ->
                foodViewModel.assignShelf(food, if (assign) shelf.id else null)
            }
        )
    }
}

@Composable
private fun ShelfRow(
    shelf: Shelf,
    onManageProducts: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = shelf.name, modifier = Modifier.weight(1f))
        IconButton(onClick = onManageProducts) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.manage_products_action))
        }
        IconButton(onClick = onRename) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename_action))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_action))
        }
    }
}