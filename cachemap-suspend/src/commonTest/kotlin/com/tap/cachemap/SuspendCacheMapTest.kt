package com.tap.cachemap

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspendCacheMapTest {

    @Test
    fun `can prepopulate the map on construction`() {
        val cachemap = suspendCacheMapOf(
            "Hello" to "World",
            "Foo" to "Bar",
        )

        assertEquals("World", cachemap["Hello"])
        assertEquals("Bar", cachemap["Foo"])
    }

    @Test
    fun `can insert a value and retrieve it`() = runTest {
        val cachemap = suspendCacheMapOf<String, String>()

        cachemap.put("Hello", "World")
        val result = cachemap.get("Hello")

        assertEquals("World", result)
    }

    @Test
    fun `can insert multiple entries`() = runTest {
        val cachemap = suspendCacheMapOf<String, String>()

        val insertees = mapOf(
            "Hello" to "World",
            "Foo" to "Bar",
        )

        cachemap.putAll(insertees)

        assertEquals("World", cachemap["Hello"])
        assertEquals("Bar", cachemap["Foo"])
    }

    @Test
    fun `can remove a value`() = runTest {
        val cachemap = suspendCacheMapOf<String, String>()

        cachemap.put("Hello", "World")
        val removed = cachemap.remove("Hello")
        val result = cachemap["Hello"]

        assertEquals("World", removed)
        assertEquals(null, result)
    }

    @Test
    fun `can remove an entry`() = runTest {
        val cachemap = suspendCacheMapOf<String, String>()

        cachemap.put("Hello", "World")
        val removed = cachemap.remove("Hello", "World")
        val result = cachemap["Hello"]

        assertEquals(true, removed)
        assertEquals(null, result)
    }

    @Test
    fun `can clear the cachemap`() = runTest {
        val cachemap = suspendCacheMapOf<String, String>()

        cachemap.put("Hello", "World")
        cachemap.put("Foo", "Bar")

        cachemap.clear()

        assertEquals(null, cachemap["Hello"])
        assertEquals(null, cachemap["Foo"])
    }
}
