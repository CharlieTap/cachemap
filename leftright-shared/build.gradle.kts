import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlinter)
}

kotlin {

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.compiler.version.get().toInt()))
        vendor.set(JvmVendorSpec.matching(libs.versions.java.vendor.get()))
    }

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions {

            }
        }
    }

    targets {
        jvm()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.atomic.fu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {

            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
