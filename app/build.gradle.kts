plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.grupo6.trego"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.grupo6.trego"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Firebase (Base) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // --- CREDENTIAL MANAGER
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- CORRUTINAS (INDISPENSABLE para usar .await() en Firebase y Google) ---
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Navegación
    implementation(libs.androidx.navigation.compose)

    // Carga de imágenes (Foto de perfil)
    implementation(libs.coil.compose)
    // Firebase (Auth + Messaging)
    implementation(libs.firebase.messaging) // PARA LAS PUSH

    // Credential Manager
    implementation(libs.androidx.credentials.play.services)
    // Retrofit + Gson Converter
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

}