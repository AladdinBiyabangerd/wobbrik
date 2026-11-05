plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}
// google-services.json is git-excluded (secret config). Apply Firebase Gradle plugins only
// when it is present so clean checkouts and CI still build.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
android {
    namespace = "com.aladdin.wobbrik"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aladdin.wobbrik"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
        val backendOrigin = providers.gradleProperty("wobbrik.backendOrigin").orElse("").get()
        require(backendOrigin.none { it == '"' || it == '\\' || it.isISOControl() })
        buildConfigField("String", "BACKEND_ORIGIN", "\"$backendOrigin\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
kotlin { jvmToolchain(21) }
dependencies {
    implementation(project(":engine"))
    implementation(project(":android"))
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.foundation:foundation:1.7.6")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-auth")
}
