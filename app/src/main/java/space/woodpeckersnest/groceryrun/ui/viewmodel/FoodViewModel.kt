package space.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.woodpeckersnest.groceryrun.data.model.Food
import space.woodpeckersnest.groceryrun.data.repository.FoodRepository
import space.woodpeckersnest.groceryrun.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AllItemsDisplayMode { ALPHABETICAL, GROUPED_BY_SHELF }

// Backs both the "All Items" and "Shopping List" tabs, since they
// share the same underlying Food data (only filtered differently).
class FoodViewModel(
    private val repository: FoodRepository,
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val currencySymbol: StateFlow<String> = userPreferencesRepository.currencySymbol

    private val _displayMode = MutableStateFlow(AllItemsDisplayMode.ALPHABETICAL)
    val displayMode: StateFlow<AllItemsDisplayMode> = _displayMode.asStateFlow()

    val allFoods: StateFlow<List<Food>> = repository.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingListItems: StateFlow<List<Food>> = repository.shoppingListItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDisplayMode(mode: AllItemsDisplayMode) {
        _displayMode.value = mode
    }

    fun addFood(name: String, shelfId: Long?, price: Double) {
        viewModelScope.launch { repository.addFood(name, shelfId, price) }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch { repository.deleteFood(food) }
    }

    // Checkbox toggle in "All Items" tab.
    fun setInShoppingList(food: Food, inShoppingList: Boolean) {
        viewModelScope.launch { repository.setInShoppingList(food.id, inShoppingList) }
    }

    // "Remove after purchase" action in "Shopping List" tab -
    // simply unchecks the item, same underlying operation.
    fun removeFromShoppingList(food: Food) {
        setInShoppingList(food, false)
    }

    fun assignShelf(food: Food, shelfId: Long?) {
        viewModelScope.launch { repository.assignShelf(food.id, shelfId) }
    }

    fun updatePrice(food: Food, price: Double) {
        viewModelScope.launch { repository.updatePrice(food.id, price) }
    }

    fun incrementQuantity(food: Food) {
        viewModelScope.launch { repository.incrementQuantity(food) }
    }

    fun decrementQuantity(food: Food) {
        viewModelScope.launch { repository.decrementQuantity(food) }
    }

    fun updateQuantity(food: Food, quantity: Int) {
        viewModelScope.launch { repository.updateQuantity(food.id, quantity) }
    }
}