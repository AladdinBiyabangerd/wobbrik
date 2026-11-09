plugins { id("com.android.library"); kotlin("android"); id("com.google.devtools.ksp") }
android {
    namespace = "appbackend.wobbrik.billing"
    compileSdk = 36
    defaultConfig {
        // Product baseline: Android 7.0; Android 16 target.
        minSdk = 24
        targetSdk = 36
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
kotlin { jvmToolchain(21) }
dependencies {
    api(project(":core"))
    implementation("com.android.billingclient:billing:9.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Approved 9 November 2026: Room 2.7.2 + KSP 2.1.10-1.0.31 (display cache only, never an outbox).
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
