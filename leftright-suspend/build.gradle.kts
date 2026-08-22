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
                api(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

configure<PublishingConventionsExtension> {
    name = "leftright-suspend"
    description = "A read optimised suspending concurrency primitive for Kotlin Multiplatform"
}
