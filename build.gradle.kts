plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.atomic.fu) apply false
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
