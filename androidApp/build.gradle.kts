plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.metrolist.music"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.metrolist.music"
        minSdk = 26
        targetSdk = 36
        versionCode = 143
        versionName = "13.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(project(":shared"))

    // Core Android / Compose
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.material3)
    implementation("androidx.compose.material:material-icons-core:1.7.6")

    // Dependency Injection
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    // Media Playback
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.okhttp)
    implementation(libs.media3.datasource)

    // Image Loading
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    // Database & Storage
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore)

    // UI Utilities
    implementation(libs.shimmer)
    implementation(libs.palette)
    implementation(libs.materialKolor)

    implementation(libs.timber)
}
