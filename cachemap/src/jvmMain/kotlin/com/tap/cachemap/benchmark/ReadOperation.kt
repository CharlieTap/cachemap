package com.tap.cachemap.benchmark

import kotlinx.atomicfu.AtomicInt

fun <K,V> readOperation(
    map: Map<K,V>,
    counter: AtomicInt,
    key: K,
): () -> Unit {
    return {
        val lookup = map[key]
        counter.incrementAndGet()
    }
}
