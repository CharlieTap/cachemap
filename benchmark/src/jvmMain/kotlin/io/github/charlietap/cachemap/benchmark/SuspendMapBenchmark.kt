package io.github.charlietap.cachemap.benchmark

import io.github.charlietap.cachemap.suspendCacheMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class SuspendCacheMapBenchmark {

    private val map = suspendCacheMapOf<String, String>()

    @Setup(Level.Trial)
    fun setup() = runBlocking {
        repeat(1000) { index -> map.put("key$index", "value$index") }
    }

    @Benchmark
    fun get(blackhole: Blackhole) {
        blackhole.consume(map["key1"])
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) = runBlocking {
        blackhole.consume(map.put("key1", "value1"))
    }
}

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class SuspendMutexMapBenchmark {

    private val map = HashMap<String, String>()
    private val mutex = Mutex()

    @Setup(Level.Trial)
    fun setup() {
        repeat(1000) { index -> map["key$index"] = "value$index" }
    }

    @Benchmark
    fun get(blackhole: Blackhole) = runBlocking {
        blackhole.consume(mutex.withLock { map["key1"] })
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) = runBlocking {
        blackhole.consume(mutex.withLock { map.put("key1", "value1") })
    }
}

@State(Scope.Thread)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class CoroutineBridgeBenchmark {

    @Benchmark
    fun runBlockingBridge(): Int = runBlocking { 1 }
}
