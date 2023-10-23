package com.tap.cachemap.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class RWHashMapSingleThreadedBenchmark {

    private val map = HashMap<String, String>()
    private val rwLock = ReentrantReadWriteLock()

    @Setup
    fun setup() {
        for (i in 1..1000) {
            map["key$i"] = "value$i"
        }
    }

    @Benchmark
    fun put(blackhole: Blackhole) {
        val result = rwLock.write {
            map.put("Hello", "World")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) {
        val result = rwLock.write {
            map.put("key1", "value2")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun putAll(blackhole: Blackhole) {
        val anotherMap = mapOf("Hello" to "World", "SecondKey" to "SecondValue")
        rwLock.write {
            map.putAll(anotherMap)
        }
        blackhole.consume(anotherMap)
    }

    @Benchmark
    fun get(blackhole: Blackhole) {
        val result: String? = rwLock.read {
            map["key1"]
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun getMiss(blackhole: Blackhole) {
        val result: String? = rwLock.read {
            map["Hello"]
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun remove(blackhole: Blackhole) {
        val result = rwLock.write {
            map.remove("key1")
        }
        blackhole.consume(result)
    }

    @Benchmark
    fun stressTest(blackhole: Blackhole) {
        for (i in 1..1000) {

            val putResult = rwLock.write {
                map.put("newKey$i", "newValue$i")
            }
            blackhole.consume(putResult)

            val getResult: String? = rwLock.read {
                map["key$i"]
            }
            blackhole.consume(getResult)

            val removeResult = rwLock.write {
                map.remove("newKey$i")
            }
            blackhole.consume(removeResult)
        }
    }

    @TearDown
    fun tearDown() {
        map.clear()
    }
}
