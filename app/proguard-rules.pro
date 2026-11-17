# ===================================================================
# Mosque Seba Enterprise ProGuard & R8 Security Rules
# ===================================================================

# Room Database Security & Entity Preservation
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.migration.Migration { *; }

# Jetpack Compose Optimization
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Firebase & App Check
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Mosque Seba Data Models
-keep class com.mosqueseba.app.data.local.entity.** { *; }
-keep class com.mosqueseba.app.data.model.** { *; }
-keep class com.mosqueseba.app.data.updater.** { *; }
-keep class com.mosqueseba.app.data.remote.** { *; }

# Obfuscate internal implementation details
-repackageclasses 'com.mosqueseba.app.sec'
-allowaccessmodification
