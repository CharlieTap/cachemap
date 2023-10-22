package com.tap.leftright

fun readerParallelism(provider: CoreProvider = coreProvider()): Int = maxOf(64, 4 * provider())
