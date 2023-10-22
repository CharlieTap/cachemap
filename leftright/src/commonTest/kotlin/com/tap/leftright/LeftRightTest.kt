package com.tap.leftright

import com.tap.leftright.LeftRight.Companion.LEFT
import com.tap.leftright.LeftRight.Companion.RIGHT
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class LeftRightTest {

    @Test
    fun `ensure thread local index local increments the total epoch count`() {
        val allEpochs = Array(1) { PaddedVolatileInt(0) }

        val totalEpochCount = atomic(0)

        val suspendLeftRight = LeftRight(
            constructor = { 0 },
            allEpochs = allEpochs,
            readEpochCount = totalEpochCount,
        )

        assertEquals(0, allEpochs[0].value)
        assertEquals(0, totalEpochCount.value) // before idx access is zero
        assertEquals(0, suspendLeftRight.readEpochIdx.value) // idx access
        assertEquals(1, totalEpochCount.value) // after idx access is incremented
        assertEquals(0, suspendLeftRight.readEpoch.value) // ensure read epoch count is at zero
    }

    @Test
    fun `ensure a read increments the relevant epoch counter by 2`() {
        val expectedResult = 117
        val allEpochs = Array(1) { PaddedVolatileInt(0) }

        val suspendLeftRight = LeftRight(
            constructor = { expectedResult },
            allEpochs = allEpochs,
        )

        val result = suspendLeftRight.read { it }

        assertEquals(2, allEpochs[0].value)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `ensure a write updates the switch`() {
        val expectedResult = mutableSetOf(1, 2)
        val switch = atomic(LEFT)
        val allEpochs = Array(1) { PaddedVolatileInt(0) }

        val suspendLeftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            allEpochs = allEpochs,
            switch = switch,
        )

        val readSide = suspendLeftRight.readSide
        val writeSide = suspendLeftRight.writeSide

        val result = suspendLeftRight.mutate { it.add(2) }

        assertEquals(true, result)
        assertEquals(0, allEpochs[0].value)
        assertEquals(RIGHT, switch.value) // assert the switch changed
        assertSame(readSide, suspendLeftRight.writeSide) // assert the pointers have switched sides
        assertSame(writeSide, suspendLeftRight.readSide) // assert the pointers have switched sides
        assertEquals(expectedResult, suspendLeftRight.left)
        assertEquals(expectedResult, suspendLeftRight.right)
    }
}
