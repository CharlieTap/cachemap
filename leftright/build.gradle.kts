plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.conventions.linting)
    alias(libs.plugins.conventions.publishing)
}

kotlin {

    sourceSets {

        commonMain {
            dependencies {
                api(projects.leftrightShared)
                api(libs.kotlinx.atomic.fu)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        nativeTest {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}

configure<PublishingConventionsExtension> {
    name = "leftright"
    description = "A read optimised concurrency primitive for Kotlin Multiplatform"
}
