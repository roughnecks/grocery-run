package com.woodpeckersnest.groceryrun.data.dao

import androidx.room.*
import com.woodpeckersnest.groceryrun.data.model.Food
import com.woodpeckersnest.groceryrun.data.model.Recipe
import com.woodpeckersnest.groceryrun.data.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("SELECT f.* FROM foods f INNER JOIN recipe_ingredients ri ON f.id = ri.foodId WHERE ri.recipeId = :recipeId ORDER BY f.name ASC")
    fun getIngredientsForRecipe(recipeId: Long): Flow<List<Food>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(recipeIngredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId AND foodId = :foodId")
    suspend fun deleteIngredient(recipeId: Long, foodId: Long)
}
