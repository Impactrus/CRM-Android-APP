# Final production build with obfuscation and optimization enabled
# but with critical fixes for generic signatures preservation.

# CRITICAL: Preserve generic signatures for Gson TypeToken
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Keep everything in our main packages
-keep class com.ossadkowski.crm.mobile.** { *; }
-keep interface com.ossadkowski.crm.mobile.** { *; }

# Keep Gson's TypeToken and all its anonymous subclasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes Signature

# Keep Gson serialized/deserialized data classes
-keep class com.ossadkowski.crm.mobile.data.model.** { *; }
-keep class com.ossadkowski.crm.mobile.data.cache.** { *; }
-keep class com.ossadkowski.crm.mobile.data.NetworkResult { *; }
-keep class com.ossadkowski.crm.mobile.data.api.ApiService { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# General attributes for libraries
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
