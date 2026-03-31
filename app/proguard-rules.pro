# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin serialization (DTOs)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Ktor (CIO client)
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# RSS (kxml2 / XmlPull)
-keep class org.kxml2.** { *; }
-dontwarn org.xmlpull.v1.**

# Google Generative AI
-keep class com.google.ai.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
