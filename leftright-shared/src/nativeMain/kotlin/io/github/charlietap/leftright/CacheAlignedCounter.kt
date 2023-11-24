package io.github.charlietap.leftright

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import libcounter.CacheAlignedInt
import libcounter.get_counter_value
import libcounter.increment_counter

@OptIn(ExperimentalForeignApi::class)
class NativeCacheAlignedCounter(initialValue: Int) : CacheAlignedCounter {

    private val counter = nativeHeap.alloc<CacheAlignedInt>()

    init {
        counter.value = initialValue
    }

    override fun increment(): Int {
        return increment_counter(counter.ptr)
    }

    override fun value(): Int {
        return get_counter_value(counter.ptr)
    }

    fun free() {
        nativeHeap.free(counter.rawPtr)
    }
}

actual fun counter(initial: Int): CacheAlignedCounter = NativeCacheAlignedCounter(initial)
