package io.github.charlietap.cachemap

interface SuspendCacheMap<K, V> : Map<K, V> {

    /** Unsupported because a live entries view cannot safely outlive its protected read. Use [forEach] instead. */
    override val entries: Set<Map.Entry<K, V>>

    /** Unsupported because a live keys view cannot safely outlive its protected read. Use [forEachKey] instead. */
    override val keys: Set<K>

    /** Unsupported because a live values view cannot safely outlive its protected read. Use [forEachValue] instead. */
    override val values: Collection<V>

    fun forEach(action: (key: K, value: V) -> Unit)

    fun forEachKey(action: (K) -> Unit)

    fun forEachValue(action: (V) -> Unit)

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
