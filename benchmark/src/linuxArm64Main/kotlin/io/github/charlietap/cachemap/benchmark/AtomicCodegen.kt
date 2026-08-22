package io.github.charlietap.cachemap.benchmark

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.fetchAndIncrement

private val boolean = AtomicBoolean(false)
private val integer = AtomicInt(0)

internal fun loadBoolean(): Boolean = boolean.load()

internal fun storeBoolean(value: Boolean) = boolean.store(value)

internal fun incrementInt(): Int = integer.fetchAndIncrement()

fun atomicCodegenMain() {
    storeBoolean(true)
    println("${loadBoolean()}:${incrementInt()}")
}
