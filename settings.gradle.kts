pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version ("3.15.1")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        publishAlwaysIf(!System.getenv("GITHUB_ACTIONS").isNullOrEmpty())
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven(url = "https://jitpack.io" )

    }
}

include(":benchmark")
include(":cachemap")
include(":cachemap-suspend")
include(":leftright")
include(":leftright-shared")
include(":leftright-suspend")

rootProject.name = "cachemap-multiplatform"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
