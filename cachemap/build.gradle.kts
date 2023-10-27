import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlin.benchmark)
    alias(libs.plugins.kotlinter)
    id("com.tap.publishing")
}

publishingConfig {
    name = "cachemap"
    description = "A read optimised concurrent map for Kotlin Multiplatform"
    version = libs.versions.version.name.get()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("jvmBenchmark")
    }
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
        jvm {
            val benchmark by compilations.creating
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(projects.leftright)
                implementation(libs.kotlinx.atomic.fu)
                implementation(libs.kotlinx.benchmark)
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

        val jvmBenchmark by getting {
            dependsOn(jvmMain)
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
