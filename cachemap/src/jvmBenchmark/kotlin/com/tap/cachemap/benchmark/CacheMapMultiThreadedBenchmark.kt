package com.tap.cachemap.benchmark

import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
class CacheMapMultiThreadedBenchmark: CacheMapSingleThreadBenchmark()
