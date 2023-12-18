package io.github.charlietap.cachemap.benchmark

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
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
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class RWHashMapSingleThreadedBenchmark {

    private val map = HashMap<String, String>()
    private val lock = ReentrantLock()

    @Setup()
    fun setup() {
        for (i in 1..1000) {
            map["key$i"] = "value$i"
        }
    }

    @Benchmark
    fun put(blackhole: Blackhole) {
        val result = lock.withLock {
            map.put("Hello", "World")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) {
        val result = lock.withLock {
            map.put("key1", "value2")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun putAll(blackhole: Blackhole) {
        val anotherMap = mapOf("Hello" to "World", "SecondKey" to "SecondValue")
        lock.withLock {
            map.putAll(anotherMap)
        }
        blackhole.consume(anotherMap)
    }

    @Benchmark
    fun get(blackhole: Blackhole) {
        val result: String? = lock.withLock {
            map["key1"]
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun getMiss(blackhole: Blackhole) {
        val result: String? = lock.withLock {
            map["Hello"]
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun remove(blackhole: Blackhole) {
        val result = lock.withLock {
            map.remove("key1")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun stressTest(blackhole: Blackhole) {
        for (i in 1..1000) {
            val putResult = lock.withLock {
                map.put("newKey$i", "newValue$i")
            }
            blackhole.consume(putResult)

            val getResult: String? = lock.withLock {
                map["key$i"]
            }
            blackhole.consume(getResult)

            val removeResult = lock.withLock {
                map.remove("newKey$i")
            }
            blackhole.consume(removeResult)
        }
    }

    @TearDown()
    fun tearDown() {
        map.clear()
    }
}
