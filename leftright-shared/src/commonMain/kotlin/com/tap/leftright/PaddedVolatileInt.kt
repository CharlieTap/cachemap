package com.tap.leftright

class PaddedVolatileInt(initialValue: Int) {

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

    fun get(): Int {
        return value
    }

    fun set(newValue: Int) {
        value = newValue
    }

    fun incrementAndGet(): Int {
        value += 1
        return value
    }
}
