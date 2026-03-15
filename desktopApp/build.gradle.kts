import java.util.Properties
import org.gradle.api.tasks.JavaExec

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
             "-opt-in=kotlin.RequiresOptIn"
         )
     }
}

// Generate a Config file similar to Android's BuildConfig
val generateConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/config/main/kotlin").get().asFile
    outputs.dir(outputDir)
    doLast {
        // 1. Try local.properties
        // 2. Try System Environment (for GitHub Actions)
        // 3. Default to empty
        val lastFmKey = localProperties.getProperty("LASTFM_API_KEY") 
            ?: System.getenv("LASTFM_API_KEY") 
            ?: ""
        val lastFmSecret = localProperties.getProperty("LASTFM_SECRET") 
            ?: System.getenv("LASTFM_SECRET") 
            ?: ""
        
        if (lastFmKey.isEmpty()) {
            logger.warn("Warning: LASTFM_API_KEY not found. Last.fm features will be disabled.")
        } else {
            println("Configured Last.fm with Key: ${lastFmKey.take(4)}...")
        }
        
        val configFile = outputDir.resolve("com/metrolist/desktop/BuildConfig.kt")
        configFile.parentFile.mkdirs()
        configFile.writeText("""
            package com.metrolist.desktop

            object BuildConfig {
                const val LASTFM_API_KEY = "${lastFmKey.trim()}"
                const val LASTFM_SECRET = "${lastFmSecret.trim()}"
            }
        """.trimIndent())
    }
}

// Task to prepare the resources directory for native distributions
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

// Ensure the desktop app runs with settings that work reliably on Linux Wayland compositors
// by forcing the GTK/JavaFX stack to use the X11 backend (via Xwayland) and a safe Skiko render API.
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
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,     // Debian / Ubuntu
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm,     // Fedora / RHEL / openSUSE
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage // Arch / generic (portable)
            )
            packageName = "Metrolist"
            packageVersion = "1.0.0"
            description = "Metrolist Music Player"
            copyright = "© 2026 Metrolist"
            vendor = "MetrolistGroup"

            // Declare only the JDK modules we actually need — jlink strips everything else,
            // producing a significantly smaller bundled JRE.
            modules(
                "java.base",
                "java.desktop",          // AWT/Swing — required for JFXPanel/SwingPanel
                "java.logging",          // java.util.logging
                "java.management",       // ManagementFactory — needed by Discord RPC native lib
                "java.naming",           // JNDI — used by JDBC driver SPI lookup
                "java.net.http",         // HttpClient — used by Ktor and other networking
                "java.prefs",            // Java Preferences API — settings persistence
                "java.sql",              // JDBC — required by SQLite / HistoryRepository
                "java.xml",             // XML parsing — used by various libs
                "jdk.crypto.ec",         // EC cipher suites — required for TLS with YouTube
                "jdk.unsupported",       // sun.misc.Unsafe and other internal APIs
                "jdk.unsupported.desktop" // SwingInterOpUtils — fixes JavaFX-Swing crash on Linux
            )

            // JavaFX requires access to internals that are restricted by the module system
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
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/logo.png"))
                // JavaFX/GTK requires C numeric locale; set it via system property as a best-effort
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
