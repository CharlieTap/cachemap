package io.github.charlietap.cachemap

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `can clear the cachemap`() {
        val cachemap = cacheMapOf<String, String>()

        cachemap["Hello"] = "World"
        cachemap["Foo"] = "Bar"

        cachemap.clear()

        assertEquals(null, cachemap["Hello"])
        assertEquals(null, cachemap["Foo"])
    }
}
