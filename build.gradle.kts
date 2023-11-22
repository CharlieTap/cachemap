plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.atomic.fu) apply false
    alias(libs.plugins.dokka) apply false
    id("maven-publish")
}

tasks.register("clean",Delete::class){
    delete(rootProject.layout.buildDirectory)
}
