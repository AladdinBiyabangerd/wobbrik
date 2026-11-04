plugins {
    kotlin("jvm") version "2.1.10" apply false
    kotlin("android") version "2.1.10" apply false
    id("com.android.library") version "8.13.0" apply false
}

tasks.register("ci") {
    group = "verification"
    description = "Local billing regression gate; extend when the game host is added"
    dependsOn(":core:test", ":android:assembleDebug", ":android:lintDebug")
}
