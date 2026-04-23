import java.time.Instant
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.middin.innovatie.app"
    compileSdk = 36
    // Use locally installed Build-Tools (avoids failed auto-download of an exact patch AGP requests).
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.middin.innovatie.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Refreshes when Gradle configures this module — each APK build gets a new stamp in the changelog.
        buildConfigField("String", "BUILD_TIME_ISO", "\"${Instant.now()}\"")

        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
        buildConfigField("String", "UPDATE_FEED_URL", "\"\"")
        // No leading slash; e.g. "api/v1" → …/api/v1/auth/login. Leave "" for flat paths.
        buildConfigField("String", "API_PATH_PREFIX", "\"\"")
        // Login JSON: only "username" + "password" OR only "email" + "password".
        buildConfigField("String", "API_LOGIN_FIELD", "\"username\"")
        buildConfigField("String", "PRESET_LOGIN_USER", "\"\"")
        buildConfigField("String", "PRESET_LOGIN_PASS", "\"\"")
        // Release always uses your API. Debug defaults to local sign-in (no network).
        buildConfigField("boolean", "USE_LOCAL_SIGN_IN", "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            // Emulator → host PC. Physical devices cannot use 10.0.2.2; use local sign-in or set your PC LAN IP in Settings.
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://10.0.2.2:8080\"",
            )
            buildConfigField("String", "UPDATE_FEED_URL", "\"http://10.0.2.2:8080/releases/latest\"")
            buildConfigField("boolean", "USE_LOCAL_SIGN_IN", "true")
            // Empty: no pre-filled credentials (sign in manually each time).
            buildConfigField("String", "PRESET_LOGIN_USER", "\"\"")
            buildConfigField("String", "PRESET_LOGIN_PASS", "\"\"")
            // Example: buildConfigField("String", "API_PATH_PREFIX", "\"api/v1\"")
            // Example: buildConfigField("String", "API_LOGIN_FIELD", "\"email\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.generativeai)
    implementation(libs.google.mlkit.image.labeling)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kxml2)

    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.mock)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
