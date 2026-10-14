package com.woodpeckersnest.groceryrun.data.repository

import com.woodpeckersnest.groceryrun.data.dao.MenuEntryDao
import com.woodpeckersnest.groceryrun.data.model.MenuEntry
import kotlinx.coroutines.flow.Flow

class MenuRepository(private val menuEntryDao: MenuEntryDao) {

    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<MenuEntry>> =
        menuEntryDao.getEntriesInRange(startDate, endDate)

    suspend fun addRecipeToDay(date: String, recipeId: Long) {
        menuEntryDao.insert(MenuEntry(date = date, recipeId = recipeId))
    }

    suspend fun addRecipesToDay(date: String, recipeIds: List<Long>) {
        val entries = recipeIds.map { MenuEntry(date = date, recipeId = it) }
        menuEntryDao.insertAll(entries)
    }

    suspend fun removeEntry(entry: MenuEntry) {
        menuEntryDao.delete(entry)
    }

    // Empties a single date.
    suspend fun clearDay(date: String) {
        menuEntryDao.clearDay(date)
    }

    // Empties a date range.
    suspend fun clearRange(startDate: String, endDate: String) {
        menuEntryDao.clearRange(startDate, endDate)
    }
}