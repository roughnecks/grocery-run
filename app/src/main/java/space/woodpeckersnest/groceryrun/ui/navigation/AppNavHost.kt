package space.woodpeckersnest.groceryrun.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.data.BackupManager
import space.woodpeckersnest.groceryrun.ui.components.CurrencySelectionDialog
import space.woodpeckersnest.groceryrun.ui.components.LanguageSelectionDialog
import space.woodpeckersnest.groceryrun.ui.screens.AllItemsScreen
import space.woodpeckersnest.groceryrun.ui.screens.RecipesScreen
import space.woodpeckersnest.groceryrun.ui.screens.ShelvesScreen
import space.woodpeckersnest.groceryrun.ui.screens.ShoppingListScreen
import space.woodpeckersnest.groceryrun.ui.screens.WeeklyMenuScreen
import space.woodpeckersnest.groceryrun.ui.viewmodel.AllItemsDisplayMode
import space.woodpeckersnest.groceryrun.ui.viewmodel.FoodViewModel
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Hosts all 5 screens behind a bottom navigation bar, plus a top
// app bar with export/import actions (available from any tab) so
// all app data can be moved to another device.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val foodViewModel: FoodViewModel = viewModel(factory = LocalViewModelFactory.current)
    val shoppingListViewModel: ShoppingListViewModel = viewModel(factory = LocalViewModelFactory.current)

    val displayMode by foodViewModel.displayMode.collectAsStateWithLifecycle()
    val autoAddIngredients by foodViewModel.userPreferencesRepository.autoAddRecipeIngredients.collectAsStateWithLifecycle()

    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    var showImportConfirm by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportSuccessMessage = stringResource(R.string.export_success)
    val markdownExportSuccessMessage = stringResource(R.string.markdown_export_success)
    val noShelfLabel = stringResource(R.string.no_shelf)
    val shoppingListTitle = stringResource(R.string.shopping_list_title)
    val importOverwriteWarning = stringResource(R.string.import_overwrite_warning)
    val exportFilename = stringResource(R.string.export_filename)
    val exportMarkdownFilename = stringResource(R.string.export_markdown_filename)

    // Lets the user pick where to save the backup file (SAF - no
    // storage permissions needed).
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                BackupManager.exportDatabase(context, uri)
                snackbarHostState.showSnackbar(exportSuccessMessage)
            }
        }
    }

    // Lets the user pick where to save the shopping list markdown file.
    val exportMarkdownLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val markdown = shoppingListViewModel.generateMarkdown(noShelfLabel, shoppingListTitle)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(markdown.toByteArray())
                    }
                }
                snackbarHostState.showSnackbar(markdownExportSuccessMessage)
            }
        }
    }

    // Lets the user pick a previously exported backup file to restore.
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentTab = AppTab.entries.firstOrNull { tab ->
                currentDestination?.hierarchy?.any { it.route == tab.route } == true
            } ?: AppTab.SHOPPING_LIST

            val isShoppingList = currentTab == AppTab.SHOPPING_LIST
            val isAllItems = currentTab == AppTab.ALL_ITEMS

            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(currentTab.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    if (isAllItems) {
                        IconButton(onClick = {
                            val newMode = if (displayMode == AllItemsDisplayMode.ALPHABETICAL)
                                AllItemsDisplayMode.GROUPED_BY_SHELF else AllItemsDisplayMode.ALPHABETICAL
                            foodViewModel.setDisplayMode(newMode)
                        }) {
                            val icon = if (displayMode == AllItemsDisplayMode.ALPHABETICAL)
                                Icons.Default.GridView else Icons.AutoMirrored.Filled.FormatListBulleted
                            Icon(icon, contentDescription = stringResource(R.string.toggle_display_mode))
                        }
                    } else if (isShoppingList) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options_action)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_markdown_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Description, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        exportMarkdownLauncher.launch(exportMarkdownFilename)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        exportLauncher.launch(exportFilename)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.import_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Upload, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        importLauncher.launch(arrayOf("*/*"))
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.language_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Language, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showLanguageDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.currency_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showCurrencyDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.pref_auto_add_ingredients)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        Switch(
                                            checked = autoAddIngredients,
                                            onCheckedChange = {
                                                foodViewModel.userPreferencesRepository.setAutoAddRecipeIngredients(it)
                                            }
                                        )
                                    },
                                    onClick = {
                                        foodViewModel.userPreferencesRepository.setAutoAddRecipeIngredients(!autoAddIngredients)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.info_action)) },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showHelpDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                AppTab.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.SHOPPING_LIST.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppTab.ALL_ITEMS.route) { 
                AllItemsScreen(foodViewModel = foodViewModel) 
            }
            composable(AppTab.SHOPPING_LIST.route) { ShoppingListScreen() }
            composable(AppTab.SHELVES.route) { ShelvesScreen() }
            composable(AppTab.RECIPES.route) { RecipesScreen() }
            composable(AppTab.WEEKLY_MENU.route) { WeeklyMenuScreen() }
        }
    }

    // Import overwrites ALL current data, so it's confirmed explicitly
    // before proceeding.
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text(stringResource(R.string.import_action)) },
            text = { Text(importOverwriteWarning) },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    val uri = pendingImportUri
                    if (uri != null) {
                        scope.launch {
                            BackupManager.importDatabase(context, uri)
                            BackupManager.restartApp(context)
                        }
                    }
                }) {
                    Text(stringResource(R.string.import_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) {
                    Text(stringResource(R.string.cancel_action))
                }
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = {
                showLanguageDialog = false
                foodViewModel.userPreferencesRepository.refreshDefaultCurrency()
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            userPreferencesRepository = foodViewModel.userPreferencesRepository,
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(R.string.help_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.help_markdown_desc))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.help_backup_desc))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.help_restore_desc))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.project_page_action),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri("https://github.com/roughnecks/grocery-run")
                            } catch (_: Exception) {
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.app_version_format, versionName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.close_action))
                }
            }
        )
    }
}