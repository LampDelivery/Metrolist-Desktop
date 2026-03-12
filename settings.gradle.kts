pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "FDD Maven"
            url = uri("https://maven.firstdark.dev/releases")
        }
    }
}

rootProject.name = "Metrolist"
include(":shared")
include(":androidApp")
include(":desktopApp")
