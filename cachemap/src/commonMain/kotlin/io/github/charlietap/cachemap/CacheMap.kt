package io.github.charlietap.cachemap

interface CacheMap<K, V> : Map<K, V> {

    operator fun set(key: K, value: V)

    fun put(key: K, value: V)

    fun putAll(from: Map<out K, V>)

    fun remove(key: K): V?

    fun remove(key: K, value: V): Boolean

    fun clear()
}

fun <K, V> cacheMapOf(): CacheMap<K, V> {
    return InternalCacheMap()
}

fun <K, V> cacheMapOf(vararg args: Pair<K, V>): CacheMap<K, V> {
    return InternalCacheMap<K, V>(initialPopulation = args.toMap())
}

fun <K, V> cacheMapOf(readerParallelism: Int, initialCapacity: Int, initialPopulation: Map<K, V>): CacheMap<K, V> {
    return InternalCacheMap(readerParallelism, initialCapacity, initialPopulation)
}
