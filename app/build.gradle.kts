plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.edoskop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.edoskop"
        minSdk = 26
        targetSdk = 34
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

    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets", "app/assets")
            }
        }
    }
}

dependencies {
    // Android Standard dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Authentication and Realtime Database
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)

    // Firebase UI for RecyclerView
    implementation("com.firebaseui:firebase-ui-database:8.0.1")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // PyTorch dependencies for machine learning
    implementation("org.pytorch:pytorch_android:1.10.0")
    implementation("org.pytorch:pytorch_android_torchvision:1.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // RecyclerView dependency
    implementation("androidx.recyclerview:recyclerview:1.2.1")
}

// Apply the Google Services plugin to integrate Firebase services correctly
apply(plugin = "com.google.gms.google-services")
