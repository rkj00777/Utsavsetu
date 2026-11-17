# ==============================================================================
# UtsavSetu Hardened R8 / ProGuard Anti-Reverse-Engineering Configuration
# ==============================================================================

# 1. Aggressive Obfuscation & Package Flattening
-repackageclasses 'o'
-allowaccessmodification
-overloadaggressively

# 2. Hide Original Source File Names from Decompilers (JADX / Apktool)
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. Strip Verbose & Debug Logging in Release Builds to Prevent Logcat Info Leaks
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 4. Keep Room Database Entities, DAOs, and TypeConverters
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# 5. Keep Firebase Firestore & Auth Reflection Entry Points
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}

# 6. Keep Android Manifest Entry Points (Application, Activity, Receivers)
-keep public class com.example.UtsavSetuApplication
-keep public class com.example.MainActivity
-keep public class com.example.platform.notifications.BootReceiver
-keep public class com.example.platform.notifications.ReminderBroadcastReceiver

# 7. Kotlin Coroutines & Serialization Safety
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# 8. Google Play Services Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**

# 9. ONNX Runtime Android JNI/reflection entry points
# Native ONNX Runtime code resolves Java API classes (including TensorInfo) by
# class name/signature through JNI. R8 must not remove, rename, or rewrite them.
-keep class ai.onnxruntime.** { *; }
-keep interface ai.onnxruntime.** { *; }
-keep enum ai.onnxruntime.** { *; }
