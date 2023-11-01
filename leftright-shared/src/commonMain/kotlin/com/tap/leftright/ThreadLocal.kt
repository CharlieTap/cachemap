package com.tap.leftright

interface ThreadLocal<T> {
    var value: T
}

expect fun <T> threadLocal(initializer: () -> T): ThreadLocal<T>
