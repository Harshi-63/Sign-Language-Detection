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
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit Text Recognition for Real-Time Translation
    implementation(libs.text.recognition)
    implementation(libs.tensorflow.lite.task.text)
    implementation(libs.tensorflow.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.task.vision)

    //For permissions
    implementation(libs.accompanist.permissions)

    // TensorFlow Lite Core Library
    implementation (libs.tensorflow.lite)


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
dependencies {
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.coil.compose)
    implementation(libs.okhttp)
    implementation (libs.androidx.core.ktx.v1120)
    implementation (libs.okhttp.v4110)
    implementation (libs.androidx.lifecycle.runtime.ktx.v261)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.json)

}
dependencies {
    // MediaPipe Tasks Vision Library (for image/text/object detection)
    implementation(libs.tasks.vision)
    implementation (libs.accompanist.webview)
    implementation(libs.androidx.heifwriter)

}
