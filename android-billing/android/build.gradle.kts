plugins { id("com.android.library"); kotlin("android") }
android {
    namespace = "appbackend.wobbrik.billing"
    compileSdk = 36
    defaultConfig {
        // Library/SDK lower bound only. Barterune's product minSdk remains undecided (DT09).
        minSdk = 23
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
