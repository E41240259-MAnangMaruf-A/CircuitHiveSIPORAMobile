plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.sipora"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sipora"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // ===== Networking =====
    implementation("com.android.volley:volley:1.2.1")
    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation("androidx.viewpager2:viewpager2:1.0.0")

        // NETWORK
        implementation("com.android.volley:volley:1.2.1")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // 🔥 Tambahkan ini → CircleImageView
        implementation("de.hdodenhof:circleimageview:3.1.0")

        // TEST
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }

}

