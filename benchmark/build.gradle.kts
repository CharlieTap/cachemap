import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.benchmark)

    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.conventions.linting)
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

    targets.named<KotlinNativeTarget>("linuxArm64") {
        binaries.executable("atomicCodegen") {
            entryPoint = "io.github.charlietap.cachemap.benchmark.atomicCodegenMain"
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
                implementation(projects.leftrightShared)
            }
        }
    }
}
