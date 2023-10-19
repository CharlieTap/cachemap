import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlinter)
    id("maven-publish")
}

group = "com.tap.cachemap"
version = libs.versions.version.name.get()

val mainClassName = "com.tap.cachemap.MainKt" // replace with your actual main class name

// Add a task to create a 'fat' JAR - one that includes all dependencies needed for runtime.
val fatJar = tasks.register<Jar>("fatJar") {
    // Basic metadata and configuration
    archiveBaseName.set("cachemap-all")
    archiveVersion.set("0.1")

    // Setting the main class name for the manifest
    manifest {
        attributes["Main-Class"] = mainClassName
    }

    // Here's the important part: correctly gathering the compiled classes and resources
    from(kotlin.targets["jvm"].compilations["main"].output.allOutputs)

    val jvmRuntimeClasspath by configurations
    from({
        jvmRuntimeClasspath.map { if (it.isDirectory) it else zipTree(it) }
    })
    // Avoiding issues with files with the same name, like different META-INF files from different libraries
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    dependsOn(fatJar)
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
                implementation(projects.leftright)
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
