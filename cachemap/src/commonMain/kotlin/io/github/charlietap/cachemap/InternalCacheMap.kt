package io.github.charlietap.cachemap

import io.github.charlietap.leftright.LeftRight

internal class InternalCacheMap<K, V>(
    readerParallelism: Int? = null,
    initialCapacity: Int? = null,
    initialPopulation: Map<K, V>? = null,
) : Map<K, V>, CacheMap<K, V> {

    private val inner = constructor(
        readerParallelism,
        initialCapacity,
        initialPopulation,
    )

    override val entries: Set<Map.Entry<K, V>>
        get() = throw UnsupportedOperationException(
            "CacheMap.entries does not expose a safe view; use forEach instead",
        )

    override val keys: Set<K>
        get() = throw UnsupportedOperationException(
            "CacheMap.keys does not expose a safe view; use forEachKey instead",
        )

    override val size: Int
        get() = inner.read(MutableMap<K, V>::size)

    override val values: Collection<V>
        get() = throw UnsupportedOperationException(
            "CacheMap.values does not expose a safe view; use forEachValue instead",
        )

    override fun isEmpty(): Boolean = inner.read(MutableMap<K, V>::isEmpty)

    override fun get(key: K): V? {
        return inner.read { map ->
            map[key]
        }
    }

    override fun containsKey(key: K): Boolean {
        return inner.read { map ->
            map.containsKey(key)
        }
    }

    override fun containsValue(value: V): Boolean {
        return inner.read { map ->
            map.containsValue(value)
        }
    }

    override fun forEach(action: (key: K, value: V) -> Unit) {
        inner.read { map ->
            for ((key, value) in map) {
                action(key, value)
            }
        }
    }

    override fun forEachKey(action: (K) -> Unit) {
        inner.read { map ->
            map.keys.forEach(action)
        }
    }

    override fun forEachValue(action: (V) -> Unit) {
        inner.read { map ->
            map.values.forEach(action)
        }
    }

    override fun put(key: K, value: V) {
        return inner.mutate { map ->
            map[key] = value
        }
    }

    override fun putAll(from: Map<out K, V>) {
        return inner.mutate { map ->
            from.forEach { (key, value) ->
                map[key] = value
            }
        }
    }

    override fun remove(key: K): V? {
        return inner.mutate { map ->
            map.remove(key)
        }
    }

    override fun remove(key: K, value: V): Boolean {
        return inner.mutate { map ->
            val current = map[key]
            var matches = current == value

            if (current == null && value == null) {
                matches = map.containsKey(key)
            }

            if (matches) {
                map.remove(key)
                true
            } else {
                false
            }
        }
    }

    override fun set(key: K, value: V) {
        return inner.mutate { map ->
            map[key] = value
        }
    }

    override fun clear() = inner.mutate(MutableMap<K, V>::clear)

    companion object {
        fun <K, V> constructor(
            readerParallelism: Int? = null,
            capacity: Int? = null,
            population: Map<K, V>? = null,
        ): LeftRight<MutableMap<K, V>> = if (
            readerParallelism == null && capacity == null && population == null
        ) {
            LeftRight(::mutableMapOf)
        } else {
            val constructor = {
                if (capacity != null) {
                    HashMap<K, V>(capacity).apply {
                        if (population != null) putAll(population)
                    }
                } else if (population != null) {
                    HashMap<K, V>(population)
                } else {
                    HashMap<K, V>()
                }
            }
            if (readerParallelism != null) {
                LeftRight(constructor, readerParallelism)
            } else {
                LeftRight(constructor)
            }
        }
    }
}
