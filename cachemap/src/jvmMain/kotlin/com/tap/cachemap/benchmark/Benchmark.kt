package com.tap.cachemap.benchmark

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.TimeSource

fun benchmark(
    duration: Duration,
    threadPoolSize: Int,
    readOperation: () -> Unit,
    writeOperation: () -> Unit,
    readBias: ReadBias,
) {
    val executor = Executors.newFixedThreadPool(threadPoolSize) as ThreadPoolExecutor
    val startMark = TimeSource.Monotonic.markNow() + duration
    val biasRange = 1..readBias.amount
    while (startMark.hasNotPassedNow()) {
        val operation = if (Random.nextInt(1, 101) in biasRange) {
            readOperation
        } else {
            writeOperation
        }

        executor.submit(operation)
    }

    val cancelledTasks = executor.shutdownNow()
    println("Cancelled tasks size: ${cancelledTasks.size}")
    println(executor.stats())
}
