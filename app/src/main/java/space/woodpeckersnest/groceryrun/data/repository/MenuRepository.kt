package space.woodpeckersnest.groceryrun.data.repository

import android.content.Context
import space.woodpeckersnest.groceryrun.data.dao.MenuEntryDao
import space.woodpeckersnest.groceryrun.data.model.MenuEntry
import space.woodpeckersnest.groceryrun.widget.TodayMenuWidgetProvider
import kotlinx.coroutines.flow.Flow

class MenuRepository(
    private val menuEntryDao: MenuEntryDao,
    private val context: Context? = null
) {

    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<MenuEntry>> =
        menuEntryDao.getEntriesInRange(startDate, endDate)

    suspend fun addRecipeToDay(date: String, recipeId: Long) {
        menuEntryDao.insert(MenuEntry(date = date, recipeId = recipeId))
        notifyWidgetUpdate()
    }

    suspend fun addRecipesToDay(date: String, recipeIds: List<Long>) {
        val entries = recipeIds.map { MenuEntry(date = date, recipeId = it) }
        menuEntryDao.insertAll(entries)
        notifyWidgetUpdate()
    }

    suspend fun removeEntry(entry: MenuEntry) {
        menuEntryDao.delete(entry)
        notifyWidgetUpdate()
    }

    // Empties a single date.
    suspend fun clearDay(date: String) {
        menuEntryDao.clearDay(date)
        notifyWidgetUpdate()
    }

    // Empties a date range.
    suspend fun clearRange(startDate: String, endDate: String) {
        menuEntryDao.clearRange(startDate, endDate)
        notifyWidgetUpdate()
    }

    private fun notifyWidgetUpdate() {
        context?.let { TodayMenuWidgetProvider.updateAllWidgets(it) }
    }
}