package space.woodpeckersnest.groceryrun.data.repository

import space.woodpeckersnest.groceryrun.data.dao.FoodDao
import space.woodpeckersnest.groceryrun.data.model.Food
import kotlinx.coroutines.flow.Flow

// Wraps FoodDao so the ViewModel layer never talks to Room directly.
class FoodRepository(private val foodDao: FoodDao) {

    val allFoods: Flow<List<Food>> = foodDao.getAllFoods()
    val shoppingListItems: Flow<List<Food>> = foodDao.getShoppingListItems()

    suspend fun addFood(name: String, shelfId: Long?, price: Double = 0.0): Long {
        return foodDao.insert(Food(name = name, shelfId = shelfId, price = price))
    }

    suspend fun updateFood(food: Food) {
        foodDao.update(food)
    }

    suspend fun deleteFood(food: Food) {
        foodDao.delete(food)
    }

    // Called when the user checks/unchecks an item in "All Items",
    // or removes it from the "Shopping List" tab after purchase.
    suspend fun setInShoppingList(foodId: Long, inShoppingList: Boolean) {
        foodDao.setInShoppingList(foodId, inShoppingList)
    }

    suspend fun updateQuantity(foodId: Long, quantity: Int) {
        if (quantity <= 0) {
            foodDao.setInShoppingListWithQuantity(foodId, false, 1)
        } else {
            foodDao.updateQuantity(foodId, quantity)
        }
    }

    suspend fun incrementQuantity(food: Food) {
        if (!food.inShoppingList) {
            foodDao.setInShoppingListWithQuantity(food.id, true, 1)
        } else {
            foodDao.updateQuantity(food.id, food.quantity + 1)
        }
    }

    suspend fun decrementQuantity(food: Food) {
        if (food.quantity > 1) {
            foodDao.updateQuantity(food.id, food.quantity - 1)
        } else {
            foodDao.setInShoppingListWithQuantity(food.id, false, 1)
        }
    }

    suspend fun addIngredientsToShoppingList(foods: List<Food>) {
        foods.forEach { food ->
            if (!food.inShoppingList) {
                foodDao.setInShoppingListWithQuantity(food.id, true, 1)
            } else {
                foodDao.updateQuantity(food.id, food.quantity + 1)
            }
        }
    }

    suspend fun assignShelf(foodId: Long, shelfId: Long?) {
        foodDao.assignShelf(foodId, shelfId)
    }

    suspend fun updatePrice(foodId: Long, price: Double) {
        foodDao.updatePrice(foodId, price)
    }
}
