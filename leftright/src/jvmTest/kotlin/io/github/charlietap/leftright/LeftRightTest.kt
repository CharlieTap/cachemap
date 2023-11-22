package io.github.charlietap.leftright

import kotlinx.atomicfu.locks.ReentrantLock
import kotlin.test.Test
import kotlin.test.assertEquals

class LeftRightJVMTest {

    @Test
    fun `ensure only a single writer can access the write side`() {
        val writeMutex = ReentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        writeMutex.lock()
        val routine = Thread {
            leftRight.mutate {
                it.add(2)
            }
        }.apply {
            start()
        }

        Thread.sleep(10)
        assertEquals(mutableSetOf(1), leftRight.readSide)
        assertEquals(mutableSetOf(1), leftRight.writeSide)
        writeMutex.unlock()
        routine.join()
        assertEquals(mutableSetOf(1, 2), leftRight.readSide)
        assertEquals(mutableSetOf(1, 2), leftRight.writeSide)
    }

    @Test
    fun `ensure reads proceeds whilst writes are taking place`() {
        val writeMutex = ReentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        assertEquals(0, leftRight.readEpoch.value)
        writeMutex.lock()

        var result: MutableSet<Int>? = null
        var epoch = 0
        val routine = Thread {
            result = leftRight.read { it }
            epoch = leftRight.readEpoch.value
        }
        routine.run {
            start()
            join()
        }
        assertEquals(mutableSetOf(1), result)
        assertEquals(0, leftRight.readEpoch.value) // first threads epoch remains
        assertEquals(2, epoch) // spawned threads epoch increments
        writeMutex.unlock()
    }

    @Test
    fun `ensure reads increment separate counters`() {
        val writeMutex = ReentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        val routine1 = Thread {
            leftRight.read {
                it.first()
            }
        }.apply {
            start()
        }
        val routine2 = Thread {
            leftRight.read {
                it.first()
            }
        }.apply {
            start()
        }

        routine1.join()
        routine2.join()

        assertEquals(2, leftRight.allEpochs[0].value)
        assertEquals(2, leftRight.allEpochs[1].value)
    }
}
