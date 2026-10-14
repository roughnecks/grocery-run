package com.woodpeckersnest.groceryrun.data.dao

import androidx.room.*
import com.woodpeckersnest.groceryrun.data.model.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    // Only items currently checked to be purchased.
    @Query("SELECT * FROM foods WHERE inShoppingList = 1 ORDER BY name ASC")
    fun getShoppingListItems(): Flow<List<Food>>

    @Insert
    suspend fun insert(food: Food): Long

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)

    // Toggle whether an item belongs to the shopping list
    // (checking it in "All Items", or removing it after purchase).
    @Query("UPDATE foods SET inShoppingList = :inShoppingList, quantity = CASE WHEN :inShoppingList = 0 THEN 1 ELSE CASE WHEN quantity < 1 THEN 1 ELSE quantity END END WHERE id = :foodId")
    suspend fun setInShoppingList(foodId: Long, inShoppingList: Boolean)

    @Query("UPDATE foods SET quantity = :quantity WHERE id = :foodId")
    suspend fun updateQuantity(foodId: Long, quantity: Int)

    @Query("UPDATE foods SET inShoppingList = :inShoppingList, quantity = :quantity WHERE id = :foodId")
    suspend fun setInShoppingListWithQuantity(foodId: Long, inShoppingList: Boolean, quantity: Int)

    // Assign or unassign a food item to a shelf.
    @Query("UPDATE foods SET shelfId = :shelfId WHERE id = :foodId")
    suspend fun assignShelf(foodId: Long, shelfId: Long?)

    @Query("UPDATE foods SET price = :price WHERE id = :foodId")
    suspend fun updatePrice(foodId: Long, price: Double)
}