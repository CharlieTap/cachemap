package com.tap.leftright

expect class CacheAlignedMemfencedInt {
    var value: Int

    fun incrementAndGet(): Int

    companion object {
        fun factory(initial: Int): CacheAlignedMemfencedInt
    }
}
