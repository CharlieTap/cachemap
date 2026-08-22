package io.github.charlietap.cachemap.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class ConcurrentHashMapSingleThreadedBenchmark {

    private val cacheMap = ConcurrentHashMap<String, String>()
    private val anotherMap = mapOf("Hello" to "World", "SecondKey" to "SecondValue")

    @Setup(Level.Iteration)
    fun setup() {
        for (i in 1..1000) {
            cacheMap["key$i"] = "value$i"
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
    fun overwriteDistinct(thread: ThreadIndexState, blackhole: Blackhole) {
        val result = cacheMap.put(thread.key, "value2")
        blackhole.consume(result)
    }

    @Benchmark
    fun putAll(blackhole: Blackhole) {
        cacheMap.putAll(anotherMap)
        blackhole.consume(anotherMap)
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
    @OperationsPerInvocation(100)
    fun readMostly(blackhole: Blackhole) {
        repeat(99) {
            blackhole.consume(cacheMap["key1"])
        }
        blackhole.consume(cacheMap.put("key1", "value1"))
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

    @TearDown(Level.Iteration)
    fun tearDown() {
        cacheMap.clear()
    }
}
