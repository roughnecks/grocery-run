package com.woodpeckersnest.groceryrun.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.woodpeckersnest.groceryrun.data.dao.FoodDao
import com.woodpeckersnest.groceryrun.data.dao.MenuEntryDao
import com.woodpeckersnest.groceryrun.data.dao.RecipeDao
import com.woodpeckersnest.groceryrun.data.dao.ShelfDao
import com.woodpeckersnest.groceryrun.data.model.Food
import com.woodpeckersnest.groceryrun.data.model.MenuEntry
import com.woodpeckersnest.groceryrun.data.model.Recipe
import com.woodpeckersnest.groceryrun.data.model.RecipeIngredient
import com.woodpeckersnest.groceryrun.data.model.Shelf
import java.time.DayOfWeek
import java.time.LocalDate

// Single Room database for the whole app. All data is stored locally
// on the device (no cloud sync), consistent with a self-hosted /
// privacy-first approach.
@Database(
    entities = [Food::class, Shelf::class, Recipe::class, MenuEntry::class, RecipeIngredient::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun shelfDao(): ShelfDao
    abstract fun recipeDao(): RecipeDao
    abstract fun menuEntryDao(): MenuEntryDao

    companion object {
        const val DATABASE_NAME = "grocery_run.db"

        // Manual migration from version 1 (no prices) to version 2 (with prices).
        // This prevents data loss when updating the app structure.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE foods ADD COLUMN price REAL NOT NULL DEFAULT 0.0")
            }
        }

        // Refactor menu entries to use specific date instead of a 2-week rotating schedule.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `menu_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `recipeId` INTEGER NOT NULL, 
                        FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent()
                )

                val currentMonday = LocalDate.now().with(DayOfWeek.MONDAY)
                db.query("SELECT id, weekIndex, dayIndex, recipeId FROM menu_entries").use { cursor ->
                    val idIndex = cursor.getColumnIndex("id")
                    val weekIndexCol = cursor.getColumnIndex("weekIndex")
                    val dayIndexCol = cursor.getColumnIndex("dayIndex")
                    val recipeIdCol = cursor.getColumnIndex("recipeId")

                    while (cursor.moveToNext()) {
                        if (idIndex >= 0 && weekIndexCol >= 0 && dayIndexCol >= 0 && recipeIdCol >= 0) {
                            val id = cursor.getLong(idIndex)
                            val weekIndex = cursor.getInt(weekIndexCol)
                            val dayIndex = cursor.getInt(dayIndexCol)
                            val recipeId = cursor.getLong(recipeIdCol)

                            val offsetDays = if (weekIndex == 1) 7L + dayIndex else dayIndex.toLong()
                            val computedDate = currentMonday.plusDays(offsetDays).toString()

                            db.execSQL(
                                "INSERT INTO menu_entries_new (id, date, recipeId) VALUES (?, ?, ?)",
                                arrayOf<Any>(id, computedDate, recipeId)
                            )
                        }
                    }
                }

                db.execSQL("DROP TABLE menu_entries")
                db.execSQL("ALTER TABLE menu_entries_new RENAME TO menu_entries")
            }
        }

        // Add quantity column to foods table.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE foods ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1")
            }
        }

        // Add recipe_ingredients join table.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `recipe_ingredients` (
                        `recipeId` INTEGER NOT NULL, 
                        `foodId` INTEGER NOT NULL, 
                        PRIMARY KEY(`recipeId`, `foodId`), 
                        FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`foodId`) REFERENCES `foods`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipe_ingredients_recipeId` ON `recipe_ingredients` (`recipeId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipe_ingredients_foodId` ON `recipe_ingredients` (`foodId`)")
            }
        }

        // Add missing foreign key indices for foods.shelfId and menu_entries.recipeId
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_foods_shelfId` ON `foods` (`shelfId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_entries_recipeId` ON `menu_entries` (`recipeId`)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Closes and discards the current database instance. Used
        // before overwriting the underlying .db file during import,
        // so the old connection isn't left pointing at replaced data.
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
