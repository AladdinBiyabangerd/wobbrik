plugins { id("com.android.library"); kotlin("android") }
android {
    namespace = "appbackend.wobbrik.billing"
    compileSdk = 36
    defaultConfig {
        // Product baseline: Android 7.0; Android 16 target.
        minSdk = 24
        targetSdk = 36
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
kotlin { jvmToolchain(21) }
dependencies {
    api(project(":core"))
    implementation("com.android.billingclient:billing:9.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
