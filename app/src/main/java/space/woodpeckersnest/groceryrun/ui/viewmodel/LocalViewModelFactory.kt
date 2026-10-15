package space.woodpeckersnest.groceryrun.ui.viewmodel

import androidx.compose.runtime.compositionLocalOf

// Provides the shared ViewModelFactory down the Compose tree, so
// every screen can create its ViewModels via
// `viewModel(factory = LocalViewModelFactory.current)` without
// manually threading it through every composable's parameters.
val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> {
    error("No ViewModelFactory provided - wrap content in CompositionLocalProvider")
}