# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin serialization (DTOs)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Ktor (CIO client)
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# RSS uses Android platform XmlPull (no bundled kxml2 — avoids R8 clash with framework types).
-dontwarn org.xmlpull.v1.**

# Google Generative AI (Ktor stubs referenced on JVM / optional OkHttp paths — not used on Android CIO client)
-dontwarn io.ktor.client.network.sockets.SocketTimeoutException
-dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
-dontwarn io.ktor.client.plugins.HttpTimeout
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Config
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Plugin
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation
-dontwarn io.ktor.utils.io.CoroutinesKt
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# Google Generative AI
-keep class com.google.ai.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
