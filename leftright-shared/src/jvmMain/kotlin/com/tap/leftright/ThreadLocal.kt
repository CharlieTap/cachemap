package com.tap.leftright

actual class ThreadLocal<T> actual constructor(private val initializer: () -> T) {
    private val threadLocal = java.lang.ThreadLocal.withInitial(initializer)

    actual var value: T
        get() = threadLocal.get()
        set(value) = threadLocal.set(value)
}
