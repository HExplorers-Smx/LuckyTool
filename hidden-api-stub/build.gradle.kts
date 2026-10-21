plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.luckyzyx.internal"
    compileSdk {
        version = release(rootProject.extra.get("compileSdkVersion") as Int) {
minorApiLevel = 2
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(rootProject.extra.get("jdkVersion") as Int)
}

dependencies {
    implementation(libs.androidx.annotation)
}
