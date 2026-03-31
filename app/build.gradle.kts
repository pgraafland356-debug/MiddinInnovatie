import java.time.Instant

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.middin.innovatie.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.middin.innovatie.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Refreshes when Gradle configures this module — each APK build gets a new stamp in the changelog.
        buildConfigField("String", "BUILD_TIME_ISO", "\"${Instant.now()}\"")

        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
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
            buildConfigField("boolean", "USE_LOCAL_SIGN_IN", "true")
            buildConfigField("String", "PRESET_LOGIN_USER", "\"pieter-bas\"")
            buildConfigField("String", "PRESET_LOGIN_PASS", "\"admin\"")
            // Example: buildConfigField("String", "API_PATH_PREFIX", "\"api/v1\"")
            // Example: buildConfigField("String", "API_LOGIN_FIELD", "\"email\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
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
    val camerax = "1.4.1"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.mlkit:image-labeling:17.0.9")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("net.sf.kxml:kxml2:2.3.0")

    testImplementation(libs.junit)
    testImplementation("io.ktor:ktor-client-mock:2.3.12")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
