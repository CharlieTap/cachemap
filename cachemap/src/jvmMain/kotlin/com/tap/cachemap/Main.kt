package com.tap.cachemap

import com.tap.cachemap.benchmark.ReadBias
import com.tap.cachemap.benchmark.benchmark
import com.tap.cachemap.benchmark.readOperation
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

fun main() {
    val benchmarkDuration = 3.seconds

    val key = "key"
    val value = "value"

    val threadPoolSize = 100
    val bias = ReadBias(100)

    val cachemap = cacheMapOf(key to value)
    val controlMap = ConcurrentHashMap(mapOf(key to value))

    val cacheMapReads = atomic(0)
    val cacheMapWrites = atomic(0)

    val controlReads = atomic(0)
    val controlWrites = atomic(0)

    val cacheMapReadOperation = readOperation(cachemap, cacheMapReads, key)
    val cacheMapWriteOperation = {
        cachemap.put(key, value)
        cacheMapWrites.incrementAndGet()
        Unit
    }

    val controlReadOperation = readOperation(controlMap, controlReads, key)
    val controlWriteOperation = {
        controlMap.put(key, value)
        controlWrites.incrementAndGet()
        Unit
    }

    benchmark(
        benchmarkDuration,
        threadPoolSize,
        cacheMapReadOperation,
        cacheMapWriteOperation,
        bias,
    )

//    benchmark(
//        benchmarkDuration,
//        threadPoolSize,
//        controlReadOperation,
//        controlWriteOperation,
//        bias,
//    )

    println("Cachemap reads: ${cacheMapReads.value}")
    println("Cachemap writes: ${cacheMapWrites.value}")
    println("Cachemap total: ${cacheMapWrites.value + cacheMapReads.value}")
    println("Control map reads: ${controlReads.value}")
    println("Control map writes: ${controlWrites.value}")
    println("Control map total: ${controlWrites.value + controlReads.value}")
}
