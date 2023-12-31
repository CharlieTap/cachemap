package io.github.charlietap.leftright

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

class SuspendLeftRight<T : Any>(
    constructor: () -> T,
    readerParallelism: Int = readerParallelism(),
    @PublishedApi internal val switch: AtomicBoolean = atomic(LEFT),
    internal val allEpochs: Array<CacheAlignedCounter> = Array(readerParallelism) { counter(0) },
    internal val readEpochCount: AtomicInt = atomic(0),
    internal val readEpochIdx: ReadEpochIndex = readEpochIndex { readEpochCount.getAndIncrement() },
    internal val left: T = constructor(),
    internal val right: T = constructor(),
    @PublishedApi internal val writeMutex: Mutex = Mutex(),
) {

    @PublishedApi
    internal val readEpoch get() = allEpochs[readEpochIdx.value()]

    @PublishedApi
    internal val readSide get() = if (switch.value == LEFT) left else right

    @PublishedApi
    internal val writeSide get() = if (switch.value == LEFT) right else left

    suspend inline fun <V> mutate(crossinline update: (T) -> V): V {
        return writeMutex.withLock {
            update(writeSide)

            switch.update { !it }

            waitForReaders()

            update(writeSide)
        }
    }

    inline fun <V> read(action: (T) -> V): V {
        readEpoch.increment()

        return action(readSide).also {
            readEpoch.increment()
        }
    }

    @PublishedApi
    internal suspend fun waitForReaders() {
        val activeThreads = readEpochCount.value

        // filter those that are odd because this signifies that they are mid-read
        // this is a little inefficient as it could also filter readers that are on the correct side but mid-read
        // Its unclear whether the computational effort of filtering these out would have any meaningful impact
        val activeIndices = (0 until activeThreads).fold(mutableListOf<Pair<Int, Int>>()) { acc, idx ->
            val epoch = allEpochs[idx].value()
            acc.apply {
                if (epoch % 2 != 0) {
                    acc.add(idx to epoch)
                }
            }
        }

        // wait for the epochs to change
        var iterations = 0

        while (activeIndices.size > 0) {
            if (iterations > 20) {
                iterations = 0
                yield()
            } else {
                activeIndices.removeAll { (allEpochsIdx, epoch) ->
                    epoch != allEpochs[allEpochsIdx].value()
                }
            }

            iterations++
        }
    }

    companion object {
        internal const val LEFT = true
        internal const val RIGHT = false
    }
}
