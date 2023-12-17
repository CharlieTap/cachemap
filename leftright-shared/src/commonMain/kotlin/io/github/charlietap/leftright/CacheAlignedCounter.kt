package io.github.charlietap.leftright

interface CacheAlignedCounter {

    fun increment(): Int

    fun value(): Int
}

expect fun counter(initial: Int): CacheAlignedCounter
