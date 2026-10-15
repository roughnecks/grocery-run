package space.woodpeckersnest.groceryrun

import android.content.Context
import space.woodpeckersnest.groceryrun.data.AppDatabase
import space.woodpeckersnest.groceryrun.data.repository.FoodRepository
import space.woodpeckersnest.groceryrun.data.repository.MenuRepository
import space.woodpeckersnest.groceryrun.data.repository.RecipeRepository
import space.woodpeckersnest.groceryrun.data.repository.ShelfRepository
import space.woodpeckersnest.groceryrun.data.repository.UserPreferencesRepository

// Minimal manual DI container - no Hilt/Dagger needed for an app
// of this size. Created once in the Application class and passed
// down to ViewModels via a factory.
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val foodRepository = FoodRepository(database.foodDao())
    val shelfRepository = ShelfRepository(database.shelfDao())
    val recipeRepository = RecipeRepository(database.recipeDao())
    val menuRepository = MenuRepository(database.menuEntryDao())
    val userPreferencesRepository = UserPreferencesRepository(context)
}