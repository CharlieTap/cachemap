import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
}

benchmark {
    targets {
        register("jvm")
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
                implementation(projects.cachemap)
                implementation(projects.cachemapSuspend)
                implementation(libs.kotlinx.atomic.fu)
                implementation(libs.kotlinx.benchmark)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.immutable.collections)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
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
