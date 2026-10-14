package com.woodpeckersnest.groceryrun.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector
import com.woodpeckersnest.groceryrun.R

// One entry per bottom-navigation tab: route name, label resource,
// and icon. Order here defines the order shown in the bottom bar.
enum class AppTab(val route: String, val labelRes: Int, val icon: ImageVector) {
    SHOPPING_LIST("shopping_list", R.string.tab_shopping_list, Icons.Default.ShoppingCart),
    ALL_ITEMS("all_items", R.string.tab_all_items, Icons.AutoMirrored.Filled.List),
    SHELVES("shelves", R.string.tab_shelves, Icons.Default.ViewWeek),
    RECIPES("recipes", R.string.tab_recipes, Icons.AutoMirrored.Filled.MenuBook),
    WEEKLY_MENU("weekly_menu", R.string.tab_weekly_menu, Icons.Default.Restaurant)
}