# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Retain generic type information and annotations for reflection
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain information required for Kotlin reflection
-keepclassmembers class kotlin.Metadata { *; }

# Moshi Keep Rules
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit Keep Rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp Keep Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Preserve our API data models and DTOs from obfuscation by R8
-keep class com.example.vietnam_travel_itinerary_android.data.dto.** { *; }
-keepclassmembers class com.example.vietnam_travel_itinerary_android.data.dto.** { *; }

-keep class com.example.vietnam_travel_itinerary_android.data.model.** { *; }
-keepclassmembers class com.example.vietnam_travel_itinerary_android.data.model.** { *; }

-keep class com.example.vietnam_travel_itinerary_android.ui.itinerary.UpdateItineraryRequest { *; }
-keepclassmembers class com.example.vietnam_travel_itinerary_android.ui.itinerary.UpdateItineraryRequest { *; }