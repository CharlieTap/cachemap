package io.github.charlietap.leftright

interface ReadEpochIndex {
    fun value(): Int
}

expect fun readEpochIndex(initializer: () -> Int): ReadEpochIndex
