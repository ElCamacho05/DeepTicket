plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"

}

android {
    namespace = "com.example.deepticket"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.deepticket"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended")

    // --- LIBRERÍAS PARA CÁMARA (CameraX) ---
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // --- LIBRERÍA PARA OCR (ML Kit Text Recognition) ---
    // Usamos la versión "play-services" que es más ligera porque usa la IA que ya trae Google Play
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // --- LIBRERÍA PARA ICONOS EXTRAS ---
    // La necesitaremos para el botón de "tomar foto"
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // LÍNEA PARA EL LOGIN
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}