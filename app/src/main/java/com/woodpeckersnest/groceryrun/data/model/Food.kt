package com.woodpeckersnest.groceryrun.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// A single food item in the master ("All Items") list.
// - shelfId: optional shelf/category assignment (nullable = unassigned)
// - inShoppingList: true when the item has been checked to be bought.
//   This single flag drives both the "Shopping List" tab membership
//   and its removal (unchecking sets it back to false).
@Entity(
    tableName = "foods",
    foreignKeys = [
        ForeignKey(
            entity = Shelf::class,
            parentColumns = ["id"],
            childColumns = ["shelfId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shelfId: Long? = null,
    val inShoppingList: Boolean = false,
    val price: Double = 0.0,
    val quantity: Int = 1
)