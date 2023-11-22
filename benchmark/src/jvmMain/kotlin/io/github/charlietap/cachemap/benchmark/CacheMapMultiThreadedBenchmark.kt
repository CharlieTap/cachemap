package io.github.charlietap.cachemap.benchmark

import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
class CacheMapMultiThreadedBenchmark : CacheMapSingleThreadBenchmark()
