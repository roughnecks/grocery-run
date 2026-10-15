package space.woodpeckersnest.groceryrun

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import space.woodpeckersnest.groceryrun.ui.navigation.AppNavHost
import space.woodpeckersnest.groceryrun.ui.theme.GroceryRunTheme
import space.woodpeckersnest.groceryrun.ui.viewmodel.LocalViewModelFactory
import space.woodpeckersnest.groceryrun.ui.viewmodel.ViewModelFactory

// Single-activity app: sets up the theme, provides the shared
// ViewModelFactory to the whole Compose tree via
// LocalViewModelFactory, and hosts AppNavHost.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as GroceryRunApplication).container
        val viewModelFactory = ViewModelFactory(container)

        setContent {
            GroceryRunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalViewModelFactory provides viewModelFactory) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}