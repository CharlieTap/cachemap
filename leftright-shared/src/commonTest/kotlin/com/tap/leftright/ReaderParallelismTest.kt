package com.tap.leftright

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderParallelismTest {

    @Test
    fun `core count multiplied by 4 is returned if larger than 64`() {
        val provider = CoreProvider { 17 } // * 4 = 68
        val result = readerParallelism(provider)

        assertEquals(68, result)
    }

    @Test
    fun `core count multiplied by 4 is not returned if less than 64`() {
        val provider = CoreProvider { 15 } // * 4 = 60
        val result = readerParallelism(provider)

        assertEquals(64, result)
    }
}
