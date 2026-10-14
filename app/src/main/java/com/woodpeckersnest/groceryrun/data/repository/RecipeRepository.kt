package com.woodpeckersnest.groceryrun.data.repository

import com.woodpeckersnest.groceryrun.data.dao.RecipeDao
import com.woodpeckersnest.groceryrun.data.model.Food
import com.woodpeckersnest.groceryrun.data.model.Recipe
import com.woodpeckersnest.groceryrun.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun addRecipe(name: String) {
        recipeDao.insert(Recipe(name = name))
    }

    suspend fun renameRecipe(recipe: Recipe, newName: String) {
        recipeDao.update(recipe.copy(name = newName))
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

    fun getIngredientsForRecipe(recipeId: Long): Flow<List<Food>> {
        return recipeDao.getIngredientsForRecipe(recipeId)
    }

    suspend fun addIngredientToRecipe(recipeId: Long, foodId: Long) {
        recipeDao.insertIngredient(RecipeIngredient(recipeId = recipeId, foodId = foodId))
    }

    suspend fun removeIngredientFromRecipe(recipeId: Long, foodId: Long) {
        recipeDao.deleteIngredient(recipeId = recipeId, foodId = foodId)
    }
}
