package com.woodpeckersnest.groceryrun.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// A "shelf" represents a category used to organize food items
// (e.g. "1fruit", "2grocery"). The numeric prefix in the name is a
// user convention to control sort order, since shelves are always
// displayed/queried in alphabetical order by name.
@Entity(tableName = "shelves")
data class Shelf(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)