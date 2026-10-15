package space.woodpeckersnest.groceryrun.data.dao

import androidx.room.*
import space.woodpeckersnest.groceryrun.data.model.MenuEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuEntryDao {
    // Entries in a given date range (inclusive), used to build the weekly view.
    @Query("SELECT * FROM menu_entries WHERE date >= :startDate AND date <= :endDate")
    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<MenuEntry>>

    @Insert
    suspend fun insert(entry: MenuEntry): Long

    @Insert
    suspend fun insertAll(entries: List<MenuEntry>)

    @Delete
    suspend fun delete(entry: MenuEntry)

    // Clear every recipe assigned to a single date.
    @Query("DELETE FROM menu_entries WHERE date = :date")
    suspend fun clearDay(date: String)

    // Clear entries within a given date range.
    @Query("DELETE FROM menu_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun clearRange(startDate: String, endDate: String)
}