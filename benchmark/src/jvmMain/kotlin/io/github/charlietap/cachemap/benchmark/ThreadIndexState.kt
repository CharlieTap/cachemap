package io.github.charlietap.cachemap.benchmark

import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.ThreadParams

@State(Scope.Thread)
class ThreadIndexState {

    var index: Int = 0

    lateinit var key: String

    @Setup(Level.Trial)
    fun setup(thread: ThreadParams) {
        index = thread.threadIndex
        key = "thread-$index"
    }
}
