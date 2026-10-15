package space.woodpeckersnest.groceryrun.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.ui.components.AddRecipeIngredientsToCartDialog
import space.woodpeckersnest.groceryrun.ui.components.AddRecipeToDayDialog
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.MenuDayEntry
import space.woodpeckersnest.groceryrun.ui.viewmodel.RecipeViewModel
import space.woodpeckersnest.groceryrun.ui.viewmodel.WeeklyMenuViewModel
import java.time.format.DateTimeFormatter

// "Weekly Menu" tab: perpetual weekly calendar view.
// Each day can hold multiple recipes (no meal distinction).
// Supports navigating between weeks and clearing a single day or an entire week range.
@Composable
fun WeeklyMenuScreen(
    menuViewModel: WeeklyMenuViewModel = viewModel(factory = LocalViewModelFactory.current),
    recipeViewModel: RecipeViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val entriesByDate by menuViewModel.entriesByDate.collectAsStateWithLifecycle()
    val recipes by recipeViewModel.allRecipes.collectAsStateWithLifecycle()
    val currentMonday by menuViewModel.currentMonday.collectAsStateWithLifecycle()
    val weekRangeLabel by menuViewModel.weekRangeLabel.collectAsStateWithLifecycle()
    val isCurrentWeek by menuViewModel.isCurrentWeek.collectAsStateWithLifecycle()
    val pendingRecipeIngredients by menuViewModel.pendingRecipeIngredients.collectAsStateWithLifecycle()
    val dayNames = stringArrayResource(R.array.day_names)
    val locale = LocalConfiguration.current.locales[0]
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM", locale) }

    var dayPickerFor by remember { mutableStateOf<String?>(null) } // date string or null

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { menuViewModel.goToPreviousWeek() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week")
            }
            Text(
                text = weekRangeLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { menuViewModel.goToNextWeek() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCurrentWeek) {
                TextButton(onClick = { menuViewModel.goToCurrentWeek() }) {
                    Icon(Icons.Default.Today, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.go_to_today_action))
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            TextButton(onClick = { menuViewModel.clearWeek() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.clear_week_action))
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(7) { dayIndex ->
                val date = currentMonday.plusDays(dayIndex.toLong())
                val dateStr = date.toString()
                val dayName = dayNames.getOrElse(dayIndex) { "" }
                val displayDayName = "$dayName (${date.dayOfMonth} ${date.format(monthFormatter)})"

                DayCard(
                    dayName = displayDayName,
                    entries = entriesByDate[dateStr].orEmpty(),
                    onAddRecipe = { dayPickerFor = dateStr },
                    onRemoveEntry = { menuDayEntry -> menuViewModel.removeEntry(menuDayEntry.entry) }
                ) {
                    menuViewModel.clearDay(dateStr)
                }
            }
        }
    }

    dayPickerFor?.let { dateStr ->
        AddRecipeToDayDialog(
            recipes = recipes,
            onDismiss = { dayPickerFor = null },
            onConfirm = { selectedRecipes ->
                menuViewModel.addRecipesToDay(dateStr, selectedRecipes.map { it.id })
                dayPickerFor = null
            }
        )
    }

    pendingRecipeIngredients?.let { pending ->
        AddRecipeIngredientsToCartDialog(
            recipeNames = pending.recipeNames,
            ingredients = pending.ingredients,
            onDismiss = { menuViewModel.dismissPendingIngredientsDialog() },
            onConfirm = { selectedFoods ->
                menuViewModel.addSelectedIngredientsToCart(selectedFoods)
            }
        )
    }
}

@Composable
private fun DayCard(
    dayName: String,
    entries: List<MenuDayEntry>,
    onAddRecipe: () -> Unit,
    onRemoveEntry: (MenuDayEntry) -> Unit,
    onClearDay: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddRecipe) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_recipe_to_day_title))
                }
                IconButton(onClick = onClearDay) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.clear_day_action))
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.day_empty),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                entries.forEach { menuDayEntry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = menuDayEntry.recipeName, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveEntry(menuDayEntry) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete_action))
                        }
                    }
                }
            }
        }
    }
}