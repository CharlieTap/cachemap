package io.github.charlietap.leftright

actual fun coreProvider() = CoreProvider {
    Runtime.getRuntime().availableProcessors()
}
