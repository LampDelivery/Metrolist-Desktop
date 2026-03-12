import java.util.Properties

val javafxVersion = "21"
val platform = org.gradle.internal.os.OperatingSystem.current().let {
    when {
        it.isWindows -> "win"
        it.isMacOsX -> "mac"
        it.isLinux -> "linux"
        else -> error("Unknown OS")
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.9.24"
}

group = "com.metrolist"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.runtime)

    implementation("com.materialkolor:material-kolor:1.7.1")
    implementation("sh.calvin.reorderable:reorderable:2.3.1")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    implementation("dev.firstdark.discordrpc:discord-rpc:1.0.3")
    implementation("com.google.code.gson:gson:2.10.1")
    
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
    
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
     kotlinOptions {
         jvmTarget = "21"
         freeCompilerArgs += listOf(
             "-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.9.24",
             "-Xopt-in=kotlin.RequiresOptIn"
         )
     }
}

// Generate a Config file similar to Android's BuildConfig
val generateConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/config/main/kotlin").get().asFile
    outputs.dir(outputDir)
    doLast {
        val lastFmKey = localProperties.getProperty("LASTFM_API_KEY") ?: System.getenv("LASTFM_API_KEY") ?: ""
        val lastFmSecret = localProperties.getProperty("LASTFM_SECRET") ?: System.getenv("LASTFM_SECRET") ?: ""
        
        val configFile = outputDir.resolve("com/metrolist/desktop/BuildConfig.kt")
        configFile.parentFile.mkdirs()
        configFile.writeText("""
            package com.metrolist.desktop

            object BuildConfig {
                const val LASTFM_API_KEY = "$lastFmKey"
                const val LASTFM_SECRET = "$lastFmSecret"
            }
        """.trimIndent())
    }
}

compose.desktop {
    application {
        mainClass = "com.metrolist.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Metrolist"
            packageVersion = "1.0.0"
            description = "Metrolist Music Player"
            copyright = "© 2026 Metrolist"
            vendor = "MetrolistGroup"
            
            modules("javafx.controls", "javafx.graphics", "javafx.web", "javafx.swing", "javafx.media")

            appResources {
                from(rootProject.file("external")) {
                    include("*.dll")
                    include("*.so")
                    include("*.dylib")
                }
            }

            windows {
                menu = true
                shortcut = true
                iconFile.set(project.file("src/jvmMain/resources/logo.svg"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/logo.svg"))
            }
            macOS {
                bundleID = "com.metrolist.desktop"
                iconFile.set(project.file("src/jvmMain/resources/logo.svg"))
            }
        }
    }
}

sourceSets {
    main {
        kotlin.srcDirs("src/jvmMain/kotlin", generateConfig)
        resources.srcDirs("src/jvmMain/resources")
    }
}
