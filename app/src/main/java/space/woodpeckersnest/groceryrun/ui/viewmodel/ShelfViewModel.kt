package space.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.woodpeckersnest.groceryrun.data.model.Shelf
import space.woodpeckersnest.groceryrun.data.repository.ShelfRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShelfViewModel(private val repository: ShelfRepository) : ViewModel() {

    val allShelves: StateFlow<List<Shelf>> = repository.allShelves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShelf(name: String) {
        viewModelScope.launch { repository.addShelf(name) }
    }

    fun renameShelf(shelf: Shelf, newName: String) {
        viewModelScope.launch { repository.renameShelf(shelf, newName) }
    }

    fun deleteShelf(shelf: Shelf) {
        viewModelScope.launch { repository.deleteShelf(shelf) }
    }
}