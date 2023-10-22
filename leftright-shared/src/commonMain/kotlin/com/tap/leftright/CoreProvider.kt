package com.tap.leftright

fun interface CoreProvider : () -> Int

expect fun coreProvider(): CoreProvider
