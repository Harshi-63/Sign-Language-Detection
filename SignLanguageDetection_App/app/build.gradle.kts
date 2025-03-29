plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Google Services for ML Kit
}

android {
    namespace = "com.example.signlanguagedetection_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.signlanguagedetection_app"
        minSdk = 29
        targetSdk = 35
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
        mlModelBinding = true
    }
}

dependencies {
    // Core AndroidX Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose Libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Firebase Authentication (if needed in your app)
    implementation(libs.firebase.auth)
    implementation(libs.tensorflow.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)

    // CameraX Dependencies for Camera Functionality
    implementation("androidx.camera:camera-core:1.5.0-alpha06")
    implementation("androidx.camera:camera-camera2:1.5.0-alpha06")
    implementation("androidx.camera:camera-lifecycle:1.5.0-alpha06")
    implementation("androidx.camera:camera-view:1.5.0-alpha06")

    // ML Kit Text Recognition for Real-Time Translation
    implementation(libs.text.recognition)
    implementation(libs.tensorflow.lite.task.text)
    implementation(libs.tensorflow.tensorflow.lite.support)
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.2")

    //For permissions
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    // TensorFlow Lite Core Library
    implementation ("com.google.mediapipe:tasks-vision:0.10.0") // MediaPipe Vision library
    implementation ("org.tensorflow:tensorflow-lite:2.11.0") // TensorFlow Lite


    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose Testing Tools
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Additional Compose Libraries (Icons, ViewModel, Navigation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel integration with Compose
    implementation(libs.androidx.navigation.compose) // Navigation for Compose


}
