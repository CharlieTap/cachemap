import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic.fu)
    id("kmp-conventions")
    id("linting-conventions")
    id("publishing-conventions")
}

fun KotlinMultiplatformExtension.unixTargets() = setOf(
    macosArm64(),
    macosX64(),
    iosArm64(),
    iosSimulatorArm64(),
    iosX64(),
    linuxArm64(),
    linuxX64(),
)

fun KotlinMultiplatformExtension.nativeTargets() = setOf(
    mingwX64()
) + unixTargets()

kotlin {

    nativeTargets().forEach {
        it.compilations.getByName("main") {
            cinterops {
                val libcounter by creating {
                    defFile(project.file("src/ffi/cinterop/libcounter.def"))
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {}
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val unixMain by creating {
            dependsOn(commonMain.get())
        }

        unixTargets().forEach { target ->
            target.compilations.getByName("main").defaultSourceSet {
                dependsOn(unixMain)
            }
        }
    }
}

configure<PublishingConventionsExtension> {
    name = "leftright-shared"
    description = "A shared runtime library exposing a read optimised concurrency primitive for Kotlin Multiplatform"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
