@file:OptIn(ExperimentalForeignApi::class)

package io.github.charlietap.leftright

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toLong
import kotlinx.cinterop.value
import platform.posix.PTHREAD_CREATE_JOINABLE
import platform.posix.pthread_attr_destroy
import platform.posix.pthread_attr_init
import platform.posix.pthread_attr_setdetachstate
import platform.posix.pthread_attr_t
import platform.posix.pthread_create
import platform.posix.pthread_join
import platform.posix.pthread_self
import platform.posix.pthread_tVar
import platform.posix.sleep
import kotlin.test.Test
import kotlin.test.assertEquals

class LeftRightNativeTest {

    private data class ThreadData(
        val leftRight: LeftRight<MutableSet<Int>>,
        var result: MutableSet<Int>? = null,
        var epoch: Int = 0,
    )

    @Test
    fun `ensure only a single writer can access the write side`() {
        val writeMutex = reentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        writeMutex.lock()

        memScoped {
            val thread = alloc<pthread_tVar>()
            val attr = alloc<pthread_attr_t>()
            pthread_attr_init(attr.ptr)
            pthread_attr_setdetachstate(attr.ptr, PTHREAD_CREATE_JOINABLE)

            pthread_create(
                thread.ptr,
                attr.ptr,
                staticCFunction { lp ->
                    val lr = lp?.asStableRef<LeftRight<MutableSet<Int>>>()?.get()
                    lr?.mutate {
                        it.add(2)
                    }
                    null
                },
                StableRef.create(leftRight).asCPointer(),
            )

            sleep(1u)

            assertEquals(mutableSetOf(1), leftRight.readSide)
            assertEquals(mutableSetOf(1), leftRight.writeSide)

            writeMutex.unlock()

            pthread_join(thread.value, null)

            assertEquals(mutableSetOf(1, 2), leftRight.readSide)
            assertEquals(mutableSetOf(1, 2), leftRight.writeSide)

            pthread_attr_destroy(attr.ptr)
        }
    }

    @Test
    fun `ensure reads proceeds whilst writes are taking place`() {
        val writeMutex = reentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        val threadData = ThreadData(
            leftRight,
        )

        assertEquals(0, leftRight.readEpoch.value())
        writeMutex.lock()

        memScoped {
            val thread = alloc<pthread_tVar>()
            val attr = alloc<pthread_attr_t>()
            pthread_attr_init(attr.ptr)
            pthread_attr_setdetachstate(attr.ptr, PTHREAD_CREATE_JOINABLE)

            pthread_create(
                thread.ptr,
                attr.ptr,
                staticCFunction { tdp ->
                    val td = tdp?.asStableRef<ThreadData>()?.get()

                    println("pthread id" + pthread_self().toLong())
                    println("pthread idx: " + td?.leftRight?.readEpochIdx?.value())
                    println("pthread value: " + td?.leftRight?.readEpoch?.value())

                    td?.result = td?.leftRight?.read { it }
                    td?.epoch = td?.leftRight?.readEpoch?.value() ?: 0
                    null
                },
                StableRef.create(threadData).asCPointer(),
            )

            pthread_join(thread.value, null)
            pthread_attr_destroy(attr.ptr)
        }

        assertEquals(mutableSetOf(1), threadData.result)
        println("test thread id" + pthread_self().toLong())
        println("test thread idx: " + leftRight.readEpochIdx.value())
        println("test thread value: " + leftRight.readEpoch.value())
        println(leftRight.allEpochs.size)
        leftRight.allEpochs.forEach {
            println(it.value())
        }
        assertEquals(0, leftRight.readEpoch.value()) // first threads epoch remains
        assertEquals(2, threadData.epoch) // spawned threads epoch increments
        writeMutex.unlock()
    }

    @Test
    fun `ensure reads increment separate counters`() {
        val writeMutex = reentrantLock()

        val leftRight = LeftRight(
            constructor = { mutableSetOf(1) },
            writeMutex = writeMutex,
        )

        memScoped {
            val thread = alloc<pthread_tVar>()
            val attr = alloc<pthread_attr_t>()
            pthread_attr_init(attr.ptr)
            pthread_attr_setdetachstate(attr.ptr, PTHREAD_CREATE_JOINABLE)

            pthread_create(
                thread.ptr,
                attr.ptr,
                staticCFunction { lp ->
                    val lr = lp?.asStableRef<LeftRight<MutableSet<Int>>>()?.get()
                    lr?.read {
                        it.first()
                    }
                    null
                },
                StableRef.create(leftRight).asCPointer(),
            )
            pthread_join(thread.value, null)
            pthread_attr_destroy(attr.ptr)
        }

        memScoped {
            val thread = alloc<pthread_tVar>()
            val attr = alloc<pthread_attr_t>()
            pthread_attr_init(attr.ptr)
            pthread_attr_setdetachstate(attr.ptr, PTHREAD_CREATE_JOINABLE)

            pthread_create(
                thread.ptr,
                attr.ptr,
                staticCFunction { lp ->
                    val lr = lp?.asStableRef<LeftRight<MutableSet<Int>>>()?.get()
                    lr?.read {
                        it.first()
                    }
                    null
                },
                StableRef.create(leftRight).asCPointer(),
            )
            pthread_join(thread.value, null)
            pthread_attr_destroy(attr.ptr)
        }

        assertEquals(2, leftRight.allEpochs[0].value())
        assertEquals(2, leftRight.allEpochs[1].value())
    }
}
