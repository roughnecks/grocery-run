# Grocery Run 🛒

**Grocery Run** (*Fai la Spesa!*) is a privacy-first, offline-first grocery list and weekly meal planning application for Android. Built with modern Android development standards using **Jetpack Compose**, **Kotlin**, **Room**, and **Material 3**.

A few screenshots (Android 9) can be found in the "media" folder.

<img src="assets/groceryrun.png" width="250" alt="Grocery Run Logo">

## Privacy & Security First

- **100% Offline & Local**: All application data is stored locally on your device in a Room SQLite database. No cloud servers, no account registration, no analytics, no tracking, and no telemetry.
- **Zero Android Permissions**: Operates without requesting any runtime Android permissions. Data export and import utilize Android's native Storage Access Framework (SAF), eliminating the need for storage permissions.
- **Broad Device Support**: Fully compatible with Android 9.0 (API 28, Pie) up to Android 16 (API 36/37).

---

## Features

### Cart / Runner (`Carrello`)
- Track items checked for purchase in real time.
- Tap-to-check / uncheck items to mark them as bought.
- Dynamic total cost calculation in real time.
- Grouped by shelf/category with rounded Material 3 surface headers.

### Product Catalog (`Lista`)
- Master list of all food items with shelf/category assignment and unit prices.
- Quantity controls (`+` / `-`) for multi-unit purchases.
- Instant search bar with quick clear button.
- Display mode toggle: **Alphabetical** or **Grouped by Shelf**.
- Price editing dialog.

### Shelves / Categories (`Scaffali`)
- Create, rename, and delete custom shelves/categories.
- Manage products assigned to each shelf.

### Recipes & Optional Ingredients (`Ricette`)
- Create, rename, and delete custom recipes.
- **Optional Ingredient List**:
  - Add ingredients to any recipe with smart auto-catalog linking.
  - If an ingredient already exists in the master catalog, it is linked automatically.
  - If the ingredient is new, prompts a prefilled dialog to optionally set its shelf and price.
  - Individual ingredient removal from recipes without deleting the food item from the master catalog.

### Weekly Menu Planner (`Menù`)
- Perpetual 7-day calendar navigation.
- **Smart Today Navigation**: A `Today` button automatically appears when navigating away from the current week.
- Assign multiple recipes to each day of the week.
- **Selective Ingredient Cart Import**: When adding recipes to a day, an optional popup lets you select which ingredients to send directly to your shopping list (excluding items you already have at home).
- Global preference switch in the 3-dots menu to enable or disable ingredient cart prompts.

### Multi-Language Support (5 Languages)
- Native support for **English**, **Italian**, **German**, **French**, and **Spanish**.
- Per-app language preference selector (Android 13+ native & Android 9–12 backward compatible).

### Multi-Currency Support
- Automatic currency symbol selection based on active language (`$` for English, `€` for Italian, German, French, Spanish).
- Manual currency override option (`€`, `$`, `£`, `CHF`, `¥`) saved in user preferences.

### Data Export & Backup
- **Export to Markdown**: Export your active cart as a formatted Markdown (`.md`) text file.
- **Database Backup & Restore**: Export or import your entire application database.

---

## Building from Source

### Prerequisites
- Android Studio (2024.1.1 or newer recommended)
- JDK 17+

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/roughnecks/grocery-run.git
   cd grocery-run
   ```

2. **Build the Debug APK**:
   ```bash
   # On Linux / macOS
   ./gradlew assembleDebug

   # On Windows (PowerShell)
   .\gradlew assembleDebug
   ```

3. The generated APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## Donate

Every donation is greatly appreciated.

- [LiberaPay](https://liberapay.com/roughnecks/)
- [Ko-Fi](https://ko-fi.com/roughnecks)
- [Paypal](https://www.paypal.me/simcana)

---

## License

This project is licensed under the [MIT License](LICENSE).
