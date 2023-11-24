package io.github.charlietap.leftright

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private var idx: Int? = null

actual fun readEpochIndex(initializer: () -> Int): ReadEpochIndex {
    return object : ReadEpochIndex {
        override fun value(): Int {
            if (idx == null) {
                idx = initializer()
            }
            return idx!!
        }
    }
}
