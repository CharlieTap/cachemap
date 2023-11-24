package io.github.charlietap.leftright

class JvmCacheAlignedCounter(initialValue: Int) : CacheAlignedCounter {

    @Volatile private var p1: Long = 0

    @Volatile private var p2: Long = 0

    @Volatile private var p3: Long = 0

    @Volatile private var p4: Long = 0

    @Volatile private var p5: Long = 0

    @Volatile private var p6: Long = 0

    @Volatile private var p7: Long = 0

    @Volatile private var p8: Long = 0

    @Volatile private var p9: Long = 0

    @Volatile private var p10: Long = 0

    @Volatile private var p11: Long = 0

    @Volatile private var p12: Long = 0

    @Volatile private var p13: Long = 0

    @Volatile private var p14: Long = 0

    @Volatile var value: Int = initialValue

    override fun value(): Int {
        return value
    }

    override fun increment(): Int {
        value += 1
        return value
    }
}

actual fun counter(initial: Int): CacheAlignedCounter = JvmCacheAlignedCounter(initial)
