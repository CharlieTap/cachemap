package io.github.charlietap.cachemap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CacheMapTest {

    @Test
    fun `can prepopulate the map on construction`() {
        val cachemap = cacheMapOf(
            "Hello" to "World",
            "Foo" to "Bar",
        )

        assertEquals("World", cachemap["Hello"])
        assertEquals("Bar", cachemap["Foo"])
    }

    @Test
    fun `can prepopulate a map with an explicit capacity`() {
        val cachemap = cacheMapOf(
            readerParallelism = 4,
            initialCapacity = 16,
            initialPopulation = mapOf("Hello" to "World"),
        )

        assertEquals("World", cachemap["Hello"])
    }

    @Test
    fun `can insert a value and retrieve it`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap.put("Hello", "World")
        val result = cachemap.get("Hello")

        assertEquals("World", result)
    }

    @Test
    fun `can insert a value and retrieve it using operator syntax`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap["Hello"] = "World"
        val result = cachemap["Hello"]

        assertEquals("World", result)
    }

    @Test
    fun `can insert multiple entries`() {
        val cachemap = cacheMapOf<String, String>()

        val insertees = mapOf(
            "Hello" to "World",
            "Foo" to "Bar",
        )

        cachemap.putAll(insertees)

        assertEquals("World", cachemap["Hello"])
        assertEquals("Bar", cachemap["Foo"])
    }

    @Test
    fun `can remove a value`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap["Hello"] = "World"
        val removed = cachemap.remove("Hello")
        val result = cachemap["Hello"]

        assertEquals("World", removed)
        assertEquals(null, result)
    }

    @Test
    fun `can remove an entry`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap["Hello"] = "World"
        val removed = cachemap.remove("Hello", "World")
        val result = cachemap["Hello"]

        assertEquals(true, removed)
        assertEquals(null, result)
    }

    @Test
    fun `does not remove an absent entry whose expected value is null`() {
        val cachemap = cacheMapOf<String, String?>()

        assertEquals(false, cachemap.remove("absent", null))
    }

    @Test
    fun `removes a present entry whose value is null`() {
        val cachemap = cacheMapOf<String, String?>()
        cachemap["present"] = null

        assertEquals(true, cachemap.remove("present", null))
        assertEquals(false, cachemap.containsKey("present"))
    }

    @Test
    fun `collection view properties are unsupported`() {
        val cachemap = cacheMapOf("first" to "value")

        assertFailsWith<UnsupportedOperationException> { cachemap.entries }
        assertFailsWith<UnsupportedOperationException> { cachemap.keys }
        assertFailsWith<UnsupportedOperationException> { cachemap.values }
    }

    @Test
    fun `can traverse entries keys and values safely`() {
        val cachemap = cacheMapOf("first" to "value", "second" to "other")
        val entries = mutableMapOf<String, String>()
        val keys = mutableSetOf<String>()
        val values = mutableListOf<String>()

        cachemap.forEach { key, value -> entries[key] = value }
        cachemap.forEachKey(keys::add)
        cachemap.forEachValue(values::add)

        assertEquals(mapOf("first" to "value", "second" to "other"), entries)
        assertEquals(setOf("first", "second"), keys)
        assertEquals(setOf("value", "other"), values.toSet())
    }

    @Test
    fun `can clear the cachemap`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap["Hello"] = "World"
        cachemap["Foo"] = "Bar"

        cachemap.clear()

        assertEquals(null, cachemap["Hello"])
        assertEquals(null, cachemap["Foo"])
    }
}
