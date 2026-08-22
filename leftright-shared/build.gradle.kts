import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conventions.kmp)
    alias(libs.plugins.conventions.linting)
    alias(libs.plugins.conventions.publishing)
}

kotlin {
    val nativeTargets = targets.withType<KotlinNativeTarget>()

    nativeTargets.configureEach {
        compilations.getByName("main") {
            cinterops {
                create("libcounter") {
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

        val unixMain = create("unixMain") {
            dependsOn(commonMain.get())
        }

        nativeTargets.matching { it.name != "mingwX64" }.configureEach {
            compilations.getByName("main").defaultSourceSet.dependsOn(unixMain)
        }
    }
}

configure<PublishingConventionsExtension> {
    name = "leftright-shared"
    description = "A shared runtime library exposing a read optimised concurrency primitive for Kotlin Multiplatform"
}
