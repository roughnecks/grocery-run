# ProGuard / R8 rules for Grocery Run

# Keep Room entities and model data classes to ensure field names match database columns
-keep class space.woodpeckersnest.groceryrun.data.model.** { *; }
