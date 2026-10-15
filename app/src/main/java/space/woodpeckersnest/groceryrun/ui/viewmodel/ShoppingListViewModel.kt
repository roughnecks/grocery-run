package space.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.woodpeckersnest.groceryrun.data.model.Food
import space.woodpeckersnest.groceryrun.data.repository.FoodRepository
import space.woodpeckersnest.groceryrun.data.repository.ShelfRepository
import space.woodpeckersnest.groceryrun.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// A group of shopping-list items belonging to the same shelf.
// "shelfName" is null for items with no shelf assigned - these
// are always sorted last, after every named shelf.
data class ShoppingListGroup(
    val shelfName: String?,
    val items: List<Food>
)

// Combines shopping-list items with shelf names and groups/sorts
// them alphabetically by shelf, as required when the shopping
// list is displayed/created.
class ShoppingListViewModel(
    private val foodRepository: FoodRepository,
    private val shelfRepository: ShelfRepository,
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val currencySymbol: StateFlow<String> = userPreferencesRepository.currencySymbol

    val groupedShoppingList: StateFlow<List<ShoppingListGroup>> =
        combine(
            foodRepository.shoppingListItems,
            shelfRepository.allShelves
        ) { items, shelves ->
            val shelfNameById = shelves.associate { it.id to it.name }

            items
                .groupBy { food -> shelfNameById[food.shelfId] } // null key = no shelf
                .map { (shelfName, foods) ->
                    ShoppingListGroup(
                        shelfName = shelfName,
                        items = foods.sortedBy { it.name }
                    )
                }
                .sortedWith(
                    // Named shelves first (alphabetically), unassigned group last.
                    compareBy(
                        { it.shelfName == null },
                        { it.shelfName ?: "" }
                    )
                )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Double> = foodRepository.shoppingListItems
        .map { items ->
            items.sumOf { it.price * it.quantity }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // "Remove after purchase" - simply unchecks the item so it
    // drops out of the shopping list view.
    fun markPurchased(food: Food) {
        viewModelScope.launch { foodRepository.setInShoppingList(food.id, false) }
    }

    // Generates a markdown representation of the shopping list, grouped
    // by shelf in the same order as shown in the UI.
    suspend fun generateMarkdown(noShelfLabel: String, listTitle: String): String {
        val items = foodRepository.shoppingListItems.first()
        val shelves = shelfRepository.allShelves.first()

        val shelfNameById = shelves.associate { it.id to it.name }

        val groups = items
            .groupBy { food -> shelfNameById[food.shelfId] }
            .map { (shelfName, foods) ->
                ShoppingListGroup(
                    shelfName = shelfName,
                    items = foods.sortedBy { it.name }
                )
            }
            .sortedWith(
                compareBy(
                    { it.shelfName == null },
                    { it.shelfName ?: "" }
                )
            )

        if (groups.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("# $listTitle\n\n")

        groups.forEach { group ->
            val shelfName = group.shelfName ?: noShelfLabel
            sb.append("## $shelfName\n")
            group.items.forEach { food ->
                val qtySuffix = if (food.quantity > 1) " (x${food.quantity})" else ""
                sb.append("- ${food.name}$qtySuffix\n")
            }
            sb.append("\n")
        }

        return sb.toString().trimEnd() + "\n"
    }
}