package io.github.charlietap.cachemap

interface CacheMap<K, V> : Map<K, V> {

    @Deprecated(
        message = "CacheMap.entries is unsupported because entries returns a view and this cannot safely outlive its protected read. Use forEach instead.",
        level = DeprecationLevel.ERROR,
    )
    override val entries: Set<Map.Entry<K, V>>

    @Deprecated(
        message = "CacheMap.keys is unsupported because keys returns a view and this cannot safely outlive its protected read. Use forEachKey instead.",
        level = DeprecationLevel.ERROR,
    )
    override val keys: Set<K>

    @Deprecated(
        message = "CacheMap.values is unsupported because values returns a view and this cannot safely outlive its protected read. Use forEachValue instead.",
        level = DeprecationLevel.ERROR,
    )
    override val values: Collection<V>

    fun forEach(action: (key: K, value: V) -> Unit)

    fun forEachKey(action: (K) -> Unit)

    fun forEachValue(action: (V) -> Unit)

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
