# Add project specific ProGuard rules here.
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Compose specific rules
# We rely on library-provided consumer rules for Compose.
# Only keep what is strictly necessary if you face specific crashes.
# -keep class androidx.compose.ui.** { *; } 
# -keep class androidx.compose.runtime.** { *; }
# -keep class androidx.compose.material.** { *; } # Removed to allow Icon shrinking
# -keep class androidx.compose.material3.** { *; }


# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.Dumpable {}

# ViewModel
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class androidx.lifecycle.ViewModel { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase$Builder { *; }
-keep interface androidx.room.Dao { *; }
-keep class * extends androidx.room.RoomDatabase

# --- APP SPECIFIC PROTECTION ---
# Since the app code itself is small, we keep all component classes 
# to ensure 100% stability with naming, reflection, and system interactions.
# The shrinking savings primarily come from removing unused Libraries (like Icons).

# Keep Data (Json/Parsers/Room Entities)
-keep class com.gollan.fadesleeptimer.data.** { *; }

# Keep ViewModels (Lifecycle reflection)
-keep class com.gollan.fadesleeptimer.viewmodel.** { *; }

# Keep Services (System intents)
-keep class com.gollan.fadesleeptimer.service.** { *; }

# Keep BreadcastReceivers (Manifest entries)
-keep class com.gollan.fadesleeptimer.receiver.** { *; }

# Keep Widgets (Glance reflection)
-keep class com.gollan.fadesleeptimer.widget.** { *; }
