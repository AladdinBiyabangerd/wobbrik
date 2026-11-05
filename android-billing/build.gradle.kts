plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.8" apply false
    kotlin("jvm") version "2.1.10" apply false
    kotlin("android") version "2.1.10" apply false
    id("com.android.library") version "8.13.0" apply false
}

tasks.register("ci") {
    group = "verification"
    description = "Local game engine and billing regression gate"
    dependsOn(":engine:test", ":core:test", ":android:assembleDebug", ":android:lintDebug", ":app:assembleDebug", ":app:lintDebug")
}
