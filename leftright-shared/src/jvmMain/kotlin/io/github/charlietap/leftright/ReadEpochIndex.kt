package io.github.charlietap.leftright

class JvmReadEpochIndex(initializer: () -> Int) : ReadEpochIndex {
    private val threadLocal = java.lang.ThreadLocal.withInitial(initializer)
    override fun value(): Int = threadLocal.get()
}

actual fun readEpochIndex(initializer: () -> Int): ReadEpochIndex = JvmReadEpochIndex(initializer)
