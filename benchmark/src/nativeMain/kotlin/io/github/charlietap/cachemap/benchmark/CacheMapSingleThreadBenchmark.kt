package io.github.charlietap.cachemap.benchmark

import io.github.charlietap.cachemap.cacheMapOf
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.benchmark.Warmup

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class CacheMapSingleThreadBenchmark {

    private val cacheMap = cacheMapOf<String, String>()

    @Setup()
    fun setup() {
        for (i in 1..1000) {
            cacheMap.put("key$i", "value$i")
        }
    }

    @Benchmark
    fun put(blackhole: Blackhole) {
        val result = cacheMap.put("Hello", "World")
        blackhole.consume(result)
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) {
        val result = cacheMap.put("key1", "value2")
        blackhole.consume(result)
    }

    @Benchmark
    fun putAll(blackhole: Blackhole) {
        val anotherMap = mapOf("Hello" to "World", "SecondKey" to "SecondValue")
        val result = cacheMap.putAll(anotherMap)
        blackhole.consume(result)
    }

    @Benchmark
    fun get(blackhole: Blackhole) {
        val result: String? = cacheMap["key1"]
        blackhole.consume(result)
    }

    @Benchmark
    fun getMiss(blackhole: Blackhole) {
        val result: String? = cacheMap["Hello"]
        blackhole.consume(result)
    }

    @Benchmark
    fun remove(blackhole: Blackhole) {
        val result = cacheMap.remove("key1")
        blackhole.consume(result)
    }

    @Benchmark
    fun stressTest(blackhole: Blackhole) {
        for (i in 1..1000) {
            val putResult = cacheMap.put("newKey$i", "newValue$i")
            blackhole.consume(putResult)

            val getResult: String? = cacheMap["key$i"]
            blackhole.consume(getResult)

            val removeResult = cacheMap.remove("newKey$i")
            blackhole.consume(removeResult)
        }
    }

    @TearDown()
    fun tearDown() {
        cacheMap.clear()
    }
}
