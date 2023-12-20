import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// temp fix for benchmark embedded compiler bug
buildscript {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    val kv = libs.versions.kotlin.get()
                    logger.warn("${requested.group}:${requested.name}:${requested.version} --> $kv")
                    useVersion(kv)
                }
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlin.benchmark)
    alias(libs.plugins.kotlinter)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
    annotation("kotlinx.benchmark.State")
}

benchmark {
    targets {
        register("jvm")
        register("macosArm64")
    }
}

kotlin {

    jvm()
    macosArm64()

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

    sourceSets {

       commonMain {
            dependencies {
                implementation(projects.cachemap)
                implementation(projects.cachemapSuspend)
                implementation(libs.kotlinx.atomic.fu)
                implementation(libs.kotlinx.benchmark)
                implementation(libs.kotlinx.coroutines.core)
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
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
