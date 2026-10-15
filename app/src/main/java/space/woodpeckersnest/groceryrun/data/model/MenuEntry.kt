package space.woodpeckersnest.groceryrun.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Links a recipe to a specific date within the menu.
// - date: YYYY-MM-DD
// Multiple MenuEntry rows can share the same date
// to allow several recipes on the same day (e.g. lunch + dinner),
// since meals are not distinguished per user request.
@Entity(
    tableName = "menu_entries",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
data class MenuEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val recipeId: Long
)