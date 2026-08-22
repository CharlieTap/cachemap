package io.github.charlietap.cachemap.benchmark

import io.github.charlietap.leftright.JvmCacheAlignedCounter
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicIntegerArray

@State(Scope.Benchmark)
@Fork(value = BenchmarkConfig.FORKS)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = BenchmarkConfig.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = BenchmarkConfig.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class CachePaddingBenchmark {

    private val objectPadded = Array(MAX_THREADS) { JvmCacheAlignedCounter(0) }
    private val objectUnpadded = Array(MAX_THREADS) { UnpaddedVolatileInt() }
    private val arrayDense = AtomicIntegerArray(MAX_THREADS)
    private val arrayPadded = AtomicIntegerArray(MAX_THREADS * INTS_PER_CACHE_LINE)

    @Benchmark
    fun objectPadded(thread: ThreadIndexState): Int = objectPadded[thread.index].increment()

    @Benchmark
    fun objectUnpadded(thread: ThreadIndexState): Int = objectUnpadded[thread.index].increment()

    @Benchmark
    fun arrayDense(thread: ThreadIndexState): Int = arrayDense.incrementAndGet(thread.index)

    @Benchmark
    fun arrayPadded(thread: ThreadIndexState): Int =
        arrayPadded.incrementAndGet(thread.index * INTS_PER_CACHE_LINE)

    private class UnpaddedVolatileInt {

        @Volatile private var value = 0

        fun increment(): Int {
            value += 1
            return value
        }
    }

    private companion object {
        const val MAX_THREADS = 256
        const val INTS_PER_CACHE_LINE = 32
    }
}
