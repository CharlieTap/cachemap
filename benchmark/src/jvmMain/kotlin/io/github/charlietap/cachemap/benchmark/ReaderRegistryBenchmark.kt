package io.github.charlietap.cachemap.benchmark

import io.github.charlietap.cachemap.CacheMap
import io.github.charlietap.cachemap.cacheMapOf
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class ReaderRegistryBenchmark {

    @Param("0", "18", "64")
    var registeredReaders: Int = 0

    private lateinit var cacheMap: CacheMap<String, String>
    private lateinit var stopReaders: CountDownLatch
    private lateinit var readerThreads: List<Thread>

    @Setup(Level.Trial)
    fun setup() {
        cacheMap = cacheMapOf(
            readerParallelism = 128,
            initialCapacity = 16,
            initialPopulation = mapOf("key" to "value"),
        )
        stopReaders = CountDownLatch(1)
        val readersReady = CountDownLatch(registeredReaders)
        readerThreads = List(registeredReaders) { index ->
            Thread {
                check(cacheMap["key"] == "value")
                readersReady.countDown()
                stopReaders.await()
            }.apply {
                name = "cachemap-benchmark-reader-$index"
                isDaemon = true
                start()
            }
        }
        readersReady.await()
    }

    @Benchmark
    fun overwrite(blackhole: Blackhole) {
        blackhole.consume(cacheMap.put("key", "value"))
    }

    @Benchmark
    fun steadyStateRead(blackhole: Blackhole) {
        blackhole.consume(cacheMap["key"])
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        stopReaders.countDown()
        readerThreads.forEach(Thread::join)
    }
}

@State(Scope.Thread)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class ReaderRegistrationBenchmark {

    @Benchmark
    fun constructAndFirstRead(blackhole: Blackhole) {
        val cacheMap = cacheMapOf(
            readerParallelism = 64,
            initialCapacity = 1,
            initialPopulation = mapOf("key" to "value"),
        )
        blackhole.consume(cacheMap["key"])
    }
}
