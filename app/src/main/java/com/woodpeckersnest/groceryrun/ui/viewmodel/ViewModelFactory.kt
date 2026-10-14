package com.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.woodpeckersnest.groceryrun.AppContainer

// Single factory for all app ViewModels, sourcing repositories
// from the shared AppContainer instead of a DI framework.
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(FoodViewModel::class.java) ->
                FoodViewModel(container.foodRepository, container.userPreferencesRepository) as T
            modelClass.isAssignableFrom(ShelfViewModel::class.java) ->
                ShelfViewModel(container.shelfRepository) as T
            modelClass.isAssignableFrom(RecipeViewModel::class.java) ->
                RecipeViewModel(
                    container.recipeRepository,
                    container.foodRepository,
                    container.shelfRepository
                ) as T
            modelClass.isAssignableFrom(ShoppingListViewModel::class.java) ->
                ShoppingListViewModel(
                    container.foodRepository,
                    container.shelfRepository,
                    container.userPreferencesRepository
                ) as T
            modelClass.isAssignableFrom(WeeklyMenuViewModel::class.java) ->
                WeeklyMenuViewModel(
                    container.menuRepository,
                    container.recipeRepository,
                    container.foodRepository,
                    container.userPreferencesRepository
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}