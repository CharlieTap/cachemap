plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.compiler.version.get().toInt()))
        vendor.set(JvmVendorSpec.matching(libs.versions.java.vendor.get()))
    }
}

gradlePlugin {
    plugins {
        create("com.tap.publishing") {
            id = "com.tap.publishing"
            implementationClass = "PublishingPlugin"
        }
    }
}
