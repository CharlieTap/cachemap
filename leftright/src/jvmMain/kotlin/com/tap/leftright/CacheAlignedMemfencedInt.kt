@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.tap.leftright

import jdk.internal.vm.annotation.Contended

@Contended
actual class CacheAlignedMemfencedInt(
    @Volatile actual var value: Int,
) {
    actual fun incrementAndGet(): Int {
        value += 1
        return value
    }

    actual companion object {
        actual fun factory(initial: Int): CacheAlignedMemfencedInt {
            return CacheAlignedMemfencedInt(initial)
        }
    }
}
