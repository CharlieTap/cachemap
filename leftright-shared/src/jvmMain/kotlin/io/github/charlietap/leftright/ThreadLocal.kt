package io.github.charlietap.leftright

class JvmThreadLocal<T>(initializer: () -> T) : ThreadLocal<T> {
    private val threadLocal = java.lang.ThreadLocal.withInitial(initializer)
    override var value: T
        get() = threadLocal.get()
        set(value) = threadLocal.set(value)
}

actual fun <T> threadLocal(initializer: () -> T): ThreadLocal<T> {
    return JvmThreadLocal(initializer)
}
