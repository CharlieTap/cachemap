package com.tap.leftright

actual fun coreProvider() = CoreProvider {
    Runtime.getRuntime().availableProcessors()
}
