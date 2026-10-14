package com.woodpeckersnest.groceryrun.data.repository

import com.woodpeckersnest.groceryrun.data.dao.ShelfDao
import com.woodpeckersnest.groceryrun.data.model.Shelf
import kotlinx.coroutines.flow.Flow

class ShelfRepository(private val shelfDao: ShelfDao) {

    val allShelves: Flow<List<Shelf>> = shelfDao.getAllShelves()

    suspend fun addShelf(name: String): Long {
        return shelfDao.insert(Shelf(name = name))
    }

    suspend fun renameShelf(shelf: Shelf, newName: String) {
        shelfDao.update(shelf.copy(name = newName))
    }

    suspend fun deleteShelf(shelf: Shelf) {
        shelfDao.delete(shelf)
    }
}