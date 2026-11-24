# Flutter ProGuard Rules
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.**  { *; }
-keep class io.flutter.util.**  { *; }
-keep class io.flutter.view.**  { *; }
-keep class io.flutter.**  { *; }
-keep class io.flutter.plugins.**  { *; }

# Drift & SQLite
-keep class com.simolus3.sqlite3_flutter_libs.** { *; }

# Supabase & Serialization
-keepattributes Signature
-keepattributes *Annotation*
