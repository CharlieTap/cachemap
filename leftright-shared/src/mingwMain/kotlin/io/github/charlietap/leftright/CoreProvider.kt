package io.github.charlietap.leftright

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.windows.GetSystemInfo
import platform.windows.SYSTEM_INFO

@OptIn(ExperimentalForeignApi::class)
actual fun coreProvider() = CoreProvider {
    memScoped {
        val systemInfo = nativeHeap.alloc<SYSTEM_INFO>()
        GetSystemInfo(systemInfo.ptr)
        systemInfo.dwNumberOfProcessors.toInt()
    }
}
