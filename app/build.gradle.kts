plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.animalist.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.animalist.app"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.firebase.firestore)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // API (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Penerjemah data JSON ke bentuk Java (Gson Converter)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Efek Kaca Mengkilap (Shimmer)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    // Library wajib buat manipulasi Splash Screen Android 12+
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.work:work-runtime:2.9.0")
    // Otak buat jalanin tugas di background walau aplikasi ditutup
    implementation("androidx.work:work-runtime:2.9.0")
    // FIX ERROR: Library Guava buat nyediain ListenableFuture yang hilang
    implementation("com.google.guava:guava:32.1.3-android")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}