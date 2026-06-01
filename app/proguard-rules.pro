# Add project specific ProGuard/R8 rules here.

# Preserve generic type signatures (needed by Gson TypeToken)
-keepattributes Signature

# Keep all annotations (SerializedName, etc.)
-keepattributes *Annotation*

# Keep fields annotated with @SerializedName (Gson reflection)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all Room entities (used by Gson for DB operations)
-keep class com.acoustics.calculator.data.local.entity.** { *; }

# Keep MaterialJsonDto (used by Gson for JSON deserialization)
-keep class com.acoustics.calculator.data.local.preload.MaterialPreloader$MaterialJsonDto { *; }

# Prevent R8 from inlining anonymous TypeToken classes
-keep,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowshrinking class * {
    *** getType();
}
