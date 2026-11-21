# ==============================================================================
# UtsavSetu R8 / ProGuard Configuration (Optimized for Fast Release Bundling)
# ==============================================================================

-dontoptimize
-keepattributes SourceFile,LineNumberTable

# Keep Room Database Entities, DAOs, and TypeConverters
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep Firebase Firestore & Auth Reflection Entry Points
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}

# Keep Android Manifest Entry Points (Application, Activity, Receivers)
-keep public class com.example.UtsavSetuApplication
-keep public class com.example.MainActivity
-keep public class com.example.platform.notifications.BootReceiver
-keep public class com.example.platform.notifications.ReminderBroadcastReceiver

# Kotlin Coroutines & Serialization Safety
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Google Play Services Ads (AdMob consumer rules are bundled with the AAR)
-dontwarn com.google.android.gms.ads.**
