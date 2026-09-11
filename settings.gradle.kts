rootProject.name = "root-shop-accounting"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":server")
include(":shared")
include(":composeApp")
include(":androidApp")
