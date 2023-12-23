package io.github.charlietap.leftright

import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf

actual fun coreProvider() = CoreProvider {
    sysconf(_SC_NPROCESSORS_ONLN).toInt()
}
