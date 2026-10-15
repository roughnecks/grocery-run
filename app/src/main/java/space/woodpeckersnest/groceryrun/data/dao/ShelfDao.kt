package space.woodpeckersnest.groceryrun.data.dao

import androidx.room.*
import space.woodpeckersnest.groceryrun.data.model.Shelf
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {
    // Shelves are always returned in alphabetical order by name,
    // relying on the user's own numeric-prefix naming convention
    // for custom ordering.
    @Query("SELECT * FROM shelves ORDER BY name ASC")
    fun getAllShelves(): Flow<List<Shelf>>

    @Insert
    suspend fun insert(shelf: Shelf): Long

    @Update
    suspend fun update(shelf: Shelf)

    @Delete
    suspend fun delete(shelf: Shelf)
}