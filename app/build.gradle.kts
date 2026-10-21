plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.hexplorers.splashguard"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig {
        applicationId = "com.hexplorers.splashguard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { buildConfig = true }
}
kotlin { jvmToolchain(21) }

dependencies {
    @Suppress("AvoidDuplicateDependencies")
    platform(libs.yukihook.bom).apply {
        implementation(this)
        ksp(this)
    }
    implementation(libs.yukihook.core)
    implementation(libs.yukihook.runtime.libxposed)
    ksp(libs.yukihook.compiler)
    compileOnly(libs.libxposed.api)
}
