package com.tap.cachemap

import com.tap.leftright.LeftRight

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
        get() = inner.read(MutableMap<K, V>::entries)

    override val keys: Set<K>
        get() = inner.read(MutableMap<K, V>::keys)

    override val size: Int
        get() = inner.read(MutableMap<K, V>::size)

    override val values: Collection<V>
        get() = inner.read(MutableMap<K, V>::values)

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
            map.remove(key, value)
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
                    HashMap<K, V>(capacity)
                } else {
                    HashMap<K, V>(population)
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
