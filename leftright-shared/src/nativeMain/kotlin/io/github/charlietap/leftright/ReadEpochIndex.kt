package io.github.charlietap.leftright

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private var idx: Int? = null

private class NativeReadEpochIndex(val initializer: () -> Int) : ReadEpochIndex {
    init {
        idx = null
    }

    override fun value(): Int {
        if (idx == null) {
            idx = initializer()
        }
        return idx!!
    }
}

actual fun readEpochIndex(initializer: () -> Int): ReadEpochIndex {
    return NativeReadEpochIndex(initializer)
}
