package io.github.charlietap.leftright

fun interface CoreProvider : () -> Int

expect fun coreProvider(): CoreProvider
