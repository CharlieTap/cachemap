package io.github.charlietap.cachemap

interface SuspendCacheMap<K, V> : Map<K, V> {

    suspend fun put(key: K, value: V)
    suspend fun putAll(from: Map<out K, V>)
    suspend fun remove(key: K): V?
    suspend fun remove(key: K, value: V): Boolean
    suspend fun clear()
}

fun <K, V> suspendCacheMapOf(): SuspendCacheMap<K, V> {
    return InternalSuspendCacheMap()
}

fun <K, V> suspendCacheMapOf(vararg args: Pair<K, V>): SuspendCacheMap<K, V> {
    return InternalSuspendCacheMap(initialPopulation = args.toMap())
}

fun <K, V> suspendCacheMapOf(readerParallelism: Int, initialCapacity: Int, initialPopulation: Map<K, V>): SuspendCacheMap<K, V> {
    return InternalSuspendCacheMap(readerParallelism, initialCapacity, initialPopulation)
}
