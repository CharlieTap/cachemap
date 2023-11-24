package io.github.charlietap.leftright

import io.github.charlietap.leftright.SuspendLeftRight.Companion.LEFT
import io.github.charlietap.leftright.SuspendLeftRight.Companion.RIGHT
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SuspendLeftRightTest {

    @Test
    fun `ensure thread local index local increments the total epoch count`() {
        val allEpochs = Array(1) { counter(0) }

        val totalEpochCount = atomic(0)

        val suspendLeftRight = SuspendLeftRight(
            constructor = { 0 },
            allEpochs = allEpochs,
            readEpochCount = totalEpochCount,
        )

        assertEquals(0, allEpochs[0].value())
        assertEquals(0, totalEpochCount.value) // before idx access is zero
        assertEquals(0, suspendLeftRight.readEpochIdx.value()) // idx access
        assertEquals(1, totalEpochCount.value) // after idx access is incremented
        assertEquals(0, suspendLeftRight.readEpoch.value()) // ensure read epoch count is at zero
    }

    @Test
    fun `ensure a read increments the relevant epoch counter by 2`() {
        val expectedResult = 117
        val allEpochs = Array(1) { counter(0) }

        val suspendLeftRight = SuspendLeftRight(
            constructor = { expectedResult },
            allEpochs = allEpochs,
        )

        val result = suspendLeftRight.read { it }

        assertEquals(2, allEpochs[0].value())
        assertEquals(expectedResult, result)
    }

    @Test
    fun `ensure a write updates the switch`() = runTest {
        val expectedResult = mutableSetOf(1, 2)
        val switch = atomic(LEFT)
        val allEpochs = Array(1) { counter(0) }

        val suspendLeftRight = SuspendLeftRight(
            constructor = { mutableSetOf(1) },
            allEpochs = allEpochs,
            switch = switch,
        )

        val readSide = suspendLeftRight.readSide
        val writeSide = suspendLeftRight.writeSide

        val result = suspendLeftRight.mutate { it.add(2) }

        assertEquals(true, result)
        assertEquals(0, allEpochs[0].value())
        assertEquals(RIGHT, switch.value) // assert the switch changed
        assertSame(readSide, suspendLeftRight.writeSide) // assert the pointers have switched sides
        assertSame(writeSide, suspendLeftRight.readSide) // assert the pointers have switched sides
        assertEquals(expectedResult, suspendLeftRight.left)
        assertEquals(expectedResult, suspendLeftRight.right)
    }

    @Test
    fun `ensure only a single writer can access the write side`() = runTest {
        val writeMutex = Mutex()

        val suspendLeftRight = SuspendLeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        writeMutex.lock()
        val routine = async {
            suspendLeftRight.mutate {
                it.add(2)
            }
        }
        delay(10)
        assertEquals(mutableSetOf(1), suspendLeftRight.readSide)
        assertEquals(mutableSetOf(1), suspendLeftRight.writeSide)
        writeMutex.unlock()
        routine.await()
        assertEquals(mutableSetOf(1, 2), suspendLeftRight.readSide)
        assertEquals(mutableSetOf(1, 2), suspendLeftRight.writeSide)
    }

    @Test
    fun `ensure reads proceeds whilst writes are taking place`() = runTest {
        val writeMutex = Mutex()

        val suspendLeftRight = SuspendLeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        assertEquals(0, suspendLeftRight.readEpoch.value())
        writeMutex.lock()
        val routine = async {
            suspendLeftRight.read { it }
        }
        val result = routine.await()
        assertEquals(mutableSetOf(1), result)
        assertEquals(2, suspendLeftRight.readEpoch.value()) // Note coroutines lets us run on the same thread
        writeMutex.unlock()
    }
}
