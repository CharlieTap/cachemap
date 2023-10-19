package com.tap.cachemap.benchmark

import java.util.concurrent.ThreadPoolExecutor

fun ThreadPoolExecutor.stats(): String = """
    === Executor Stats ===
    Current pool size: $poolSize")
    Number of active threads: $activeCount")
    Number of completed tasks: $completedTaskCount")
    Total number of tasks: $taskCount")
    Is the pool terminated: $isTerminated")
    === End Executor Stats ===
    """.trimIndent()
