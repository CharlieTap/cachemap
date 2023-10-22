package com.tap.cachemap.benchmark
import com.tap.cachemap.cacheMapOf
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.*

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class CacheMapSingleThreadBenchmark {

    private val cacheMap = cacheMapOf<String, String>()

    @Setup
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

    @TearDown
    fun tearDown() {
        cacheMap.clear()
    }
}
