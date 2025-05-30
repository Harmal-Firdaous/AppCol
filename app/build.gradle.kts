plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.appco"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appco"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0") // Use the latest version
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")// For Glide's annotation processor (optional but recommended)
    implementation ("com.google.firebase:firebase-database:20.0.5")

    // Google Maps and Location (keep these lines)
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // Important: Add these for better compatibility
    implementation("androidx.fragment:fragment:1.6.2")  // For SupportMapFragment
    implementation("androidx.legacy:legacy-support-v4:1.0.0")  // For backward compatibility
    implementation("com.google.android.gms:play-services-base:18.2.0")  // Base Google services


}