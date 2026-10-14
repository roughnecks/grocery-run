package com.woodpeckersnest.groceryrun.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Exports/imports the whole app database as a single file, so all
// data (foods, shelves, recipes, menu) can be moved to another
// device without re-entering anything by hand.
object BackupManager {

    // Merges Room's Write-Ahead-Log into the main .db file (so no
    // recent change is missed), then copies that single file to the
    // destination the user picked via the system file picker (SAF).
    suspend fun exportDatabase(context: Context, destinationUri: Uri) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        // Ensure all data is flushed to the main .db file. We consume the
        // cursor to ensure the PRAGMA actually executes.
        db.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).use { cursor ->
            cursor.moveToFirst()
        }

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        if (dbFile.exists()) {
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                dbFile.inputStream().use { input -> input.copyTo(output) }
            }
        }
    }

    // Replaces the current database file with the one the user picked,
    // discarding any leftover WAL/SHM files from the previous database
    // so they aren't accidentally merged with the imported data.
    suspend fun importDatabase(context: Context, sourceUri: Uri) = withContext(Dispatchers.IO) {
        AppDatabase.closeInstance()

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            dbFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    // Fully restarts the app process. Used right after an import so
    // every screen/ViewModel starts fresh against the newly imported
    // data, instead of trying to patch up live database connections.
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = launchIntent!!.component
        val restartIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }
}