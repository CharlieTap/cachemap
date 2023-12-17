import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlinter)
    id("kmp-conventions")
    id("publishing-conventions")
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

        jvmMain {
            dependencies {

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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
