package space.woodpeckersnest.groceryrun.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// A recipe is just a named entry (no ingredients tracked),
// listed alphabetically in the "Recipes" tab and assignable
// to days in the weekly menu.
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)