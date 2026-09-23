# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities
-keep class com.connectlens.app.data.local.entity.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.FragmentComponentManager { *; }

# Keep Kotlin data classes used in serialization
-keep class com.connectlens.app.domain.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# DataStore
-dontwarn androidx.datastore.**
