package space.woodpeckersnest.groceryrun

import android.app.Application

// Custom Application class - holds the single AppContainer instance
// used to obtain repositories throughout the app's lifetime.
class GroceryRunApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}