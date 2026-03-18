import java.util.Properties
import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.metrolist"
version = "1.0.1"

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

    // Updated versions for March 2026/Kotlin 2.3.10 era
    implementation("sh.calvin.reorderable:reorderable:3.0.0")
    implementation("org.xerial:sqlite-jdbc:3.48.0.0")
    implementation("net.java.dev.jna:jna:5.18.1")
    implementation("net.java.dev.jna:jna-platform:5.18.1")
    implementation("dev.firstdark.discordrpc:discord-rpc:1.0.3")
    implementation("com.google.code.gson:gson:2.11.0")
    
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
    
    val ktorVersion = "3.4.1"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
}

// Generate a Config file similar to Android's BuildConfig
val generateConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/config/main/kotlin").get().asFile

    val lastFmKeyProvider = providers.gradleProperty("LASTFM_API_KEY")
        .filter { it.isNotEmpty() }
        .orElse(providers.environmentVariable("LASTFM_API_KEY").filter { it.isNotEmpty() })
        .orElse(localProperties.getProperty("LASTFM_API_KEY").orEmpty())
    val lastFmSecretProvider = providers.gradleProperty("LASTFM_SECRET")
        .filter { it.isNotEmpty() }
        .orElse(providers.environmentVariable("LASTFM_SECRET").filter { it.isNotEmpty() })
        .orElse(localProperties.getProperty("LASTFM_SECRET").orEmpty())

    inputs.property("lastFmKey", lastFmKeyProvider)
    inputs.property("lastFmSecret", lastFmSecretProvider)
    outputs.dir(outputDir)
    outputs.cacheIf { false }

    doLast {
        val lastFmKey = lastFmKeyProvider.get().trim()
        val lastFmSecret = lastFmSecretProvider.get().trim()

        val configFile = outputDir.resolve("com/metrolist/desktop/BuildConfig.kt")
        configFile.parentFile.mkdirs()
        configFile.writeText("""
            package com.metrolist.desktop

            object BuildConfig {
                const val LASTFM_API_KEY = "${lastFmKey}"
                const val LASTFM_SECRET = "${lastFmSecret}"
                const val APP_VERSION = "1.0.1"
            }
        """.trimIndent())
    }
}

val syncExternalResources by tasks.registering(Sync::class) {
    val destination = layout.buildDirectory.dir("native-resources")
    into(destination)
    
    from(rootProject.file("external")) {
        include("*.dll")
        into("windows")
    }
    from(rootProject.file("external")) {
        include("*.so")
        into("linux")
    }
    from(rootProject.file("external")) {
        include("*.dylib")
        into("macos")
    }
}

tasks.withType<JavaExec>().configureEach {
    if (platform == "linux") {
        environment("LC_NUMERIC", "C")
        environment("GDK_BACKEND", "x11")
        environment("_JAVA_AWT_WM_NONREPARENTING", "1")
        environment("SKIKO_RENDER_API", "SOFTWARE")
    }
}

compose.desktop {
    application {
        mainClass = "com.metrolist.desktop.MainKt"
        nativeDistributions {
            val formats = when (platform) {
                "win"  -> arrayOf(TargetFormat.Msi)
                "mac"  -> arrayOf(TargetFormat.Dmg)
                else   -> arrayOf(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
            }
            targetFormats(*formats)
            packageName = "Metrolist"
            packageVersion = "1.0.1"
            description = "Metrolist Music Player"
            copyright = "© 2026 Metrolist"
            vendor = "MetrolistGroup"

            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.management",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.sql",
                "java.xml",
                "jdk.crypto.ec",
                "jdk.jsobject",
                "jdk.unsupported",
                "jdk.unsupported.desktop"
            )

            jvmArgs += listOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.swing=ALL-UNNAMED"
            )

            appResourcesRootDir.set(syncExternalResources.map { project.layout.projectDirectory.dir(it.destinationDir.absolutePath) })

            windows {
                menu = true
                shortcut = true
                iconFile.set(project.file("src/jvmMain/resources/logo.ico"))
                upgradeUuid = "a2e1b3c4-5d6f-4789-8abc-0d1e2f3a4b5c"
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/logo.png"))
                jvmArgs += listOf("-Djava.locale.providers=COMPAT,SPI")
            }
            macOS {
                bundleID = "com.metrolist.desktop"
                iconFile.set(project.file("src/jvmMain/resources/logo.icns"))
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
