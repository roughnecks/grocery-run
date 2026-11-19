package space.woodpeckersnest.groceryrun.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import space.woodpeckersnest.groceryrun.MainActivity
import space.woodpeckersnest.groceryrun.R
import space.woodpeckersnest.groceryrun.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodayMenuWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Make sure the next midnight refresh is always scheduled while widgets exist.
        scheduleMidnightUpdate(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Schedule the midnight refresh when the first widget is added.
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightUpdate(context)
    }

    // Cancel the midnight refresh when the last widget is removed.
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelMidnightUpdate(context)
    }

    // The widget shows the current and next day, but the system only calls onUpdate periodically
    // and it may skip those calls while the device is idle. The midnight alarm covers the
    // day change; the date and time broadcasts cover manual clock changes.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_MIDNIGHT_UPDATE -> {
                updateAllWidgets(context)
                // Reschedule, since a clock or timezone change moves the next midnight.
                scheduleMidnightUpdate(context)
            }
        }
    }

    companion object {
        // Private action used by the midnight alarm to trigger a refresh.
        const val ACTION_MIDNIGHT_UPDATE = "space.woodpeckersnest.groceryrun.widget.MIDNIGHT_UPDATE"
        private const val MIDNIGHT_REQUEST_CODE = 1

        private fun midnightPendingIntent(context: Context, flags: Int): PendingIntent? {
            val intent = Intent(context, TodayMenuWidgetProvider::class.java).apply {
                action = ACTION_MIDNIGHT_UPDATE
            }
            return PendingIntent.getBroadcast(
                context,
                MIDNIGHT_REQUEST_CODE,
                intent,
                flags or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Schedules a refresh at the next local midnight. The alarm is inexact and allowed
        // while idle, so it needs no special permission; it may fire a few minutes late.
        fun scheduleMidnightUpdate(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val nextMidnight = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val pendingIntent = midnightPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
                ?: return
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMidnight, pendingIntent)
        }

        fun cancelMidnightUpdate(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val pendingIntent = midnightPendingIntent(context, PendingIntent.FLAG_NO_CREATE)
                ?: return
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodayMenuWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, TodayMenuWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }

        suspend fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val today = LocalDate.now()
            val db = AppDatabase.getInstance(context)

            val todayRecipes = recipeNamesForDate(db, today.toString())
            val tomorrowRecipes = recipeNamesForDate(db, today.plusDays(1).toString())

            val dayName = today.format(
                DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_today_menu)
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                views.setTextViewText(R.id.widget_date_text, dayName)

                setRecipesText(
                    context, views, R.id.widget_recipes_text,
                    todayRecipes, R.string.widget_no_recipes
                )
                setRecipesText(
                    context, views, R.id.widget_tomorrow_recipes_text,
                    tomorrowRecipes, R.string.widget_no_recipes_tomorrow
                )

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        // Returns the names of the recipes planned for the given date, in menu order.
        private suspend fun recipeNamesForDate(db: AppDatabase, dateStr: String): List<String> {
            val entries = db.menuEntryDao().getEntriesForDate(dateStr)
            if (entries.isEmpty()) return emptyList()
            val allRecipes = db.recipeDao().getAllRecipesList().associateBy { it.id }
            return entries.mapNotNull { allRecipes[it.recipeId]?.name }
        }

        // Fills one recipe column: a bulleted list, or the empty-state message when there are no recipes.
        private fun setRecipesText(
            context: Context,
            views: RemoteViews,
            viewId: Int,
            recipes: List<String>,
            emptyResId: Int
        ) {
            if (recipes.isNotEmpty()) {
                views.setTextViewText(viewId, recipes.joinToString("\n") { "• $it" })
            } else {
                views.setTextViewText(viewId, context.getString(emptyResId))
            }
        }
    }
}