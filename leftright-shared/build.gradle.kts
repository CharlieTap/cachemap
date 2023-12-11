import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic.fu)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
    id("kmp-conventions")
}

kotlin {

    setOf(
        macosArm64(),
        macosX64(),
        iosArm64(),
        iosX64(),
        linuxArm64(),
        linuxX64(),
    ).forEach {
        it.compilations.getByName("main") {
            cinterops {
                val libcounter by creating {
                    defFile(project.file("src/ffi/cinterop/libcounter.def"))
                }
            }
        }
    }

    sourceSets {
       commonMain {
            dependencies {}
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}


val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}

tasks.withType<DokkaTask>().configureEach {
    notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

group = "io.github.charlietap"
version = libs.versions.version.name.get()

publishing {

    val manualFileRepo = uri("file://${rootProject.layout.buildDirectory.get()}/manual")

    repositories {
        maven {
            name = "manual"
            url = manualFileRepo
        }
    }

    publications.withType<MavenPublication>().configureEach {

        artifact(javadocJar)

        pom {
            name.set(project.name)
            description.set("A shared runtime library exposing a read optimised concurrency primitive for Kotlin Multiplatform")
            url.set("https://github.com/CharlieTap/cachemap")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("CharlieTap")
                    name.set("Charlie Tapping")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/CharlieTap/cachemap.git")
                developerConnection.set("scm:git:ssh://github.com/CharlieTap/cachemap.git")
                url.set("https://github.com/CharlieTap/cachemap")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if(signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    sign(project.extensions.getByType<PublishingExtension>().publications)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = libs.versions.java.bytecode.version.get()
}
