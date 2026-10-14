package com.woodpeckersnest.groceryrun.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Join table linking a Recipe to a Food item as an ingredient.
@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "foodId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["foodId"])
    ]
)
data class RecipeIngredient(
    val recipeId: Long,
    val foodId: Long
)
