package com.tap.leftright

import kotlinx.atomicfu.locks.ReentrantLock
import kotlin.test.Test
import kotlin.test.assertEquals

class LeftRightJVMTest {

    @Test
    fun `ensure only a single writer can access the write side`() {
        val writeMutex = ReentrantLock()

        val suspendLeftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        writeMutex.lock()
        val routine = Thread {
            suspendLeftRight.mutate {
                it.add(2)
            }
        }.apply {
            start()
        }

        Thread.sleep(10)
        assertEquals(mutableSetOf(1), suspendLeftRight.readSide)
        assertEquals(mutableSetOf(1), suspendLeftRight.writeSide)
        writeMutex.unlock()
        routine.join()
        assertEquals(mutableSetOf(1, 2), suspendLeftRight.readSide)
        assertEquals(mutableSetOf(1, 2), suspendLeftRight.writeSide)
    }

    @Test
    fun `ensure reads proceeds whilst writes are taking place`() {
        val writeMutex = ReentrantLock()

        val suspendLeftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        assertEquals(0, suspendLeftRight.readEpoch.value)
        writeMutex.lock()

        var result: MutableSet<Int>? = null
        var epoch = 0
        val routine = Thread {
            result = suspendLeftRight.read { it }
            epoch = suspendLeftRight.readEpoch.value
        }
        routine.run {
            start()
            join()
        }
        assertEquals(mutableSetOf(1), result)
        assertEquals(0, suspendLeftRight.readEpoch.value) // first threads epoch remains
        assertEquals(2, epoch) // spawned threads epoch increments
        writeMutex.unlock()
    }
}
