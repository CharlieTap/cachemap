import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask

plugins {
    id("org.jmailen.kotlinter")
}

val config = rootProject.file(".editorconfig")

tasks.withType<ConfigurableKtLintTask>().configureEach {
    inputs.file(config).withPathSensitivity(PathSensitivity.RELATIVE)
    exclude { it.file.path.contains("build/")}
}
