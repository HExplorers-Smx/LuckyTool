plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.luckyzyx.colorpicker"
    compileSdk {
        version = release(rootProject.extra.get("compileSdkVersion") as Int) {
minorApiLevel = 2
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(rootProject.extra.get("jdkVersion") as Int)
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.preference.ktx)
}