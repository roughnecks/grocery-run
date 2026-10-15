package space.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.woodpeckersnest.groceryrun.data.model.Food
import space.woodpeckersnest.groceryrun.data.model.MenuEntry
import space.woodpeckersnest.groceryrun.data.repository.FoodRepository
import space.woodpeckersnest.groceryrun.data.repository.MenuRepository
import space.woodpeckersnest.groceryrun.data.repository.RecipeRepository
import space.woodpeckersnest.groceryrun.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// A single recipe assignment shown on a day, carrying the
// resolved recipe name alongside the raw MenuEntry (needed to
// remove that specific assignment).
data class MenuDayEntry(
    val entry: MenuEntry,
    val recipeName: String
)

data class PendingRecipeIngredients(
    val recipeNames: String,
    val ingredients: List<Food>
)

// Combines raw menu entries with recipe names and groups them by date,
// so the UI can render a perpetual calendar week grid without doing any lookups itself.
class WeeklyMenuViewModel(
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val autoAddRecipeIngredients: StateFlow<Boolean> = userPreferencesRepository.autoAddRecipeIngredients

    private val _pendingRecipeIngredients = MutableStateFlow<PendingRecipeIngredients?>(null)
    val pendingRecipeIngredients: StateFlow<PendingRecipeIngredients?> = _pendingRecipeIngredients.asStateFlow()

    private val _currentMonday = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))
    val currentMonday: StateFlow<LocalDate> = _currentMonday.asStateFlow()

    fun goToNextWeek() {
        _currentMonday.value = _currentMonday.value.plusWeeks(1)
    }

    fun goToPreviousWeek() {
        _currentMonday.value = _currentMonday.value.minusWeeks(1)
    }

    fun goToCurrentWeek() {
        _currentMonday.value = LocalDate.now().with(DayOfWeek.MONDAY)
    }

    val isCurrentWeek: StateFlow<Boolean> = _currentMonday.map { monday ->
        monday == LocalDate.now().with(DayOfWeek.MONDAY)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val weekRangeLabel: StateFlow<String> = _currentMonday.map { monday ->
        val sunday = monday.plusDays(6)
        val d1 = monday.dayOfMonth
        val d2 = sunday.dayOfMonth
        val m1 = monday.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
        val m2 = sunday.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
        val y1 = monday.year
        val y2 = sunday.year

        if (y1 != y2) {
            "$d1 $m1 $y1 - $d2 $m2 $y2"
        } else if (m1 != m2) {
            "$d1 $m1 - $d2 $m2 $y1"
        } else {
            "$d1 - $d2 $m1 $y1"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val entriesByDate: StateFlow<Map<String, List<MenuDayEntry>>> =
        _currentMonday.flatMapLatest { monday ->
            val endDate = monday.plusDays(6).toString()
            combine(
                menuRepository.getEntriesInRange(monday.toString(), endDate),
                recipeRepository.allRecipes
            ) { entries, recipes ->
                val recipeNameById = recipes.associateBy({ it.id }, { it.name })
                entries
                    .mapNotNull { entry ->
                        val name = recipeNameById[entry.recipeId] ?: return@mapNotNull null
                        MenuDayEntry(entry, name)
                    }
                    .groupBy { it.entry.date }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun addRecipesToDay(date: String, recipeIds: List<Long>) {
        viewModelScope.launch {
            menuRepository.addRecipesToDay(date, recipeIds)

            if (autoAddRecipeIngredients.value && recipeIds.isNotEmpty()) {
                val allRecipes = recipeRepository.allRecipes.first()
                val selectedRecipes = allRecipes.filter { recipeIds.contains(it.id) }
                val recipeNames = selectedRecipes.joinToString(", ") { it.name }

                val ingredientsList = mutableListOf<Food>()
                for (recipeId in recipeIds) {
                    val ingredients = recipeRepository.getIngredientsForRecipe(recipeId).first()
                    ingredientsList.addAll(ingredients)
                }

                // Distinct by food id
                val distinctIngredients = ingredientsList.distinctBy { it.id }

                if (distinctIngredients.isNotEmpty()) {
                    _pendingRecipeIngredients.value = PendingRecipeIngredients(
                        recipeNames = recipeNames,
                        ingredients = distinctIngredients
                    )
                }
            }
        }
    }

    fun addSelectedIngredientsToCart(selectedFoods: List<Food>) {
        viewModelScope.launch {
            foodRepository.addIngredientsToShoppingList(selectedFoods)
            _pendingRecipeIngredients.value = null
        }
    }

    fun dismissPendingIngredientsDialog() {
        _pendingRecipeIngredients.value = null
    }

    fun removeEntry(entry: MenuEntry) {
        viewModelScope.launch { menuRepository.removeEntry(entry) }
    }

    fun clearDay(date: String) {
        viewModelScope.launch { menuRepository.clearDay(date) }
    }

    fun clearWeek() {
        viewModelScope.launch {
            val monday = _currentMonday.value
            val sunday = monday.plusDays(6)
            menuRepository.clearRange(monday.toString(), sunday.toString())
        }
    }
}
