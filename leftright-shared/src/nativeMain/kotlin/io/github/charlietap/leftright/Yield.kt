package io.github.charlietap.leftright

import platform.posix.sched_yield

actual fun yield() {
    sched_yield()
}
