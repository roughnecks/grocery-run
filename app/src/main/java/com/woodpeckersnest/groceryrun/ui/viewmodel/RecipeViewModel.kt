package com.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woodpeckersnest.groceryrun.data.model.Food
import com.woodpeckersnest.groceryrun.data.model.Recipe
import com.woodpeckersnest.groceryrun.data.model.Shelf
import com.woodpeckersnest.groceryrun.data.repository.FoodRepository
import com.woodpeckersnest.groceryrun.data.repository.RecipeRepository
import com.woodpeckersnest.groceryrun.data.repository.ShelfRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
    private val shelfRepository: ShelfRepository
) : ViewModel() {

    val allRecipes: StateFlow<List<Recipe>> = recipeRepository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShelves: StateFlow<List<Shelf>> = shelfRepository.allShelves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRecipeForIngredients = MutableStateFlow<Recipe?>(null)
    val selectedRecipeForIngredients: StateFlow<Recipe?> = _selectedRecipeForIngredients.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentRecipeIngredients: StateFlow<List<Food>> = _selectedRecipeForIngredients
        .flatMapLatest { recipe ->
            if (recipe == null) flowOf(emptyList())
            else recipeRepository.getIngredientsForRecipe(recipe.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // When an ingredient name does not exist in foods table,
    // this state holds the prefilled name for AddFoodDialog.
    private val _pendingNewIngredientName = MutableStateFlow<String?>(null)
    val pendingNewIngredientName: StateFlow<String?> = _pendingNewIngredientName.asStateFlow()

    fun selectRecipeForIngredients(recipe: Recipe?) {
        _selectedRecipeForIngredients.value = recipe
    }

    fun addRecipe(name: String) {
        viewModelScope.launch { recipeRepository.addRecipe(name) }
    }

    fun renameRecipe(recipe: Recipe, newName: String) {
        viewModelScope.launch { recipeRepository.renameRecipe(recipe, newName) }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe) }
    }

    // Tries to add an ingredient by name. If a matching food already exists,
    // links it directly. If not, triggers pendingNewIngredientName for AddFoodDialog.
    fun addIngredientByName(recipeId: Long, ingredientName: String) {
        val trimmedName = ingredientName.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val foods = foodRepository.allFoods.first()
            val existingFood = foods.firstOrNull { it.name.equals(trimmedName, ignoreCase = true) }

            if (existingFood != null) {
                recipeRepository.addIngredientToRecipe(recipeId, existingFood.id)
            } else {
                _pendingNewIngredientName.value = trimmedName
            }
        }
    }

    // Called after the user completes AddFoodDialog for a new ingredient.
    fun confirmNewIngredientFood(recipeId: Long, name: String, shelfId: Long?, price: Double) {
        viewModelScope.launch {
            val newFoodId = foodRepository.addFood(name = name, shelfId = shelfId, price = price)
            recipeRepository.addIngredientToRecipe(recipeId, newFoodId)
            _pendingNewIngredientName.value = null
        }
    }

    fun dismissNewIngredientDialog() {
        _pendingNewIngredientName.value = null
    }

    fun removeIngredientFromRecipe(recipeId: Long, foodId: Long) {
        viewModelScope.launch {
            recipeRepository.removeIngredientFromRecipe(recipeId, foodId)
        }
    }
}
