plugins {
    // Kotlin 1.9.24 supports JVM 21
    // Compose 1.6.10 is compatible with Kotlin 1.9.24
    kotlin("multiplatform").version("1.9.24").apply(false)
    kotlin("android").version("1.9.24").apply(false)
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    id("org.jetbrains.compose").version("1.6.10").apply(false)
}
