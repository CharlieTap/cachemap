package com.tap.leftright

expect class ThreadLocal<T>(initializer: () -> T) {
    var value: T
}
