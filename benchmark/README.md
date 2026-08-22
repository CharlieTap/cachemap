# CacheMap benchmarks

This suite covers the scenarios from [Making my concurrent algorithm 6000% better](https://dev.to/charlietap/making-my-concurrent-algorithm-6000-better-24oo) and adds the controls needed to explain the results rather than only repeat them.

## Coverage

- CacheMap, `ConcurrentHashMap`, and `HashMap` guarded by `ReentrantReadWriteLock`
- single-threaded and 18-thread reads, misses, writes, removes, bulk writes, and stress operations
- same-key and per-thread-key writes, exposing `ConcurrentHashMap` bin contention
- a 99-read/1-write read-mostly workload
- read-throughput scaling at 1, 2, 4, 8, 16, and 18 threads
- padded and unpadded counter objects
- dense and cache-line-strided `AtomicIntegerArray` controls
- `SuspendCacheMap`, a `Mutex`-guarded map, and the `runBlocking` bridge cost
- a Linux ARM64 code-generation fixture for Kotlin standard-library atomics

## 2026-08-22 migration result

Environment: Apple M5 Pro, 18 logical CPUs, 128-byte cache line, macOS 26.6.1 arm64, OpenJDK 25.0.4, JMH 1.37, Kotlin 2.4.10, and Gradle 9.7.1.

The baseline was captured after completing the benchmark suite but before changing the implementation. The post-change run used exactly the same JMH configuration: two forks, three 300 ms warmup iterations, five 500 ms measurement iterations, and 99.9% confidence intervals. Throughput is in operations per nanosecond.

### Read scaling

| Threads | CacheMap baseline | CacheMap standard atomics | Change | ConcurrentHashMap after | RW-lock map after |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 0.150 | 0.205 | +36.6% | 0.536 | 0.116 |
| 2 | 0.297 | 0.408 | +37.3% | 1.066 | 0.0122 |
| 4 | 0.554 | 0.786 | +41.8% | 2.046 | 0.00580 |
| 8 | 0.913 | 1.337 | +46.4% | 3.316 | 0.00485 |
| 16 | 1.573 | 2.319 | +47.4% | 5.641 | 0.00523 |
| 18 | 1.570 | 2.308 | +47.0% | 5.667 | 0.00469 |

CacheMap scales 11.26 times from 1 to 18 threads after the migration, or 62.5% parallel efficiency. The baseline scaled 10.46 times, or 58.1% efficiency. Both runs saturate between 16 and 18 threads, which is expected on this heterogeneous 18-core system. The `ConcurrentHashMap` control changed only +2.6% at 18 threads, so general machine drift does not explain CacheMap's +47.0% result.

### Operation comparison

| CacheMap operation | Baseline | After | Change | Interpretation |
| --- | ---: | ---: | ---: | --- |
| 18-thread read | 1.988 | 2.601 | +30.8% | Core matrix; the dedicated scaling sweep above is the cleaner comparison |
| 18-thread read miss | 2.418 | 3.658 | +51.3% | No value-return cost |
| 18-thread same-key overwrite | 0.0453 | 0.0550 | +21.3% | Still serialized; average latency fell from 425.5 to 364.8 ns |
| 18-thread distinct-key overwrite | 0.0454 | 0.0563 | +23.9% | Expected to match same-key writes because CacheMap has one writer |
| 18-thread 99:1 read/write mix | 0.1385 | 0.1565 | +13.0% | Confidence intervals overlap; no regression demonstrated |
| 18-thread `putAll` | 0.0317 | 0.0322 | +1.6% | Effectively unchanged |

The `ConcurrentHashMap` distinct-key control is materially faster than its same-key write benchmark, confirming the caveat in the article: forcing all writers into one bin understates its real distributed-write performance. CacheMap does not vary by key distribution because its write side is intentionally serialized.

### False-sharing controls

| 18-thread counter layout | Throughput | Relative to unpadded form |
| --- | ---: | ---: |
| Dense atomic int array | 0.109 | 1.0x |
| 128-byte-strided atomic int array | 3.693 | 33.8x |
| Unpadded volatile counter objects | 1.048 | 1.0x |
| Actual CacheMap padded counter objects | 9.504 | 9.1x |

At one thread, padded and unpadded forms are effectively equal. The large multithreaded separation therefore isolates false sharing, reproducing the mechanism described in the article. The strided primitive-array control is important because it guarantees address separation; an object-reference array alone cannot guarantee that its referents are contiguous.

The result should not be described as a fresh 6000% whole-map gain: the current implementation was already padded in both baseline and post-change runs. It demonstrates that removing the padding would still be disastrous on current Apple Silicon.

### Suspend variants

| Operation | Baseline | After | Change |
| --- | ---: | ---: | ---: |
| 1-thread `SuspendCacheMap` read | 0.158 | 0.215 | +36.1% |
| 18-thread `SuspendCacheMap` read | 1.772 | 2.690 | +51.8% |
| 18-thread `SuspendCacheMap` overwrite | 0.000293 | 0.000302 | +3.2% |
| 18-thread mutex-map read | 0.000296 | 0.000305 | +3.2% |
| 18-thread `runBlocking` bridge | 0.0378 | 0.0382 | +0.9% |

This fills the main gap called out by the original article, which explicitly said the suspend variants had not been benchmarked. Suspend CacheMap reads retain the lock-free read advantage; contended suspend writes remain governed by mutex scheduling and are comparable to the mutex-map control.

## Generated-code audit

The implementation now uses the experimental `kotlin.concurrent.atomics.AtomicBoolean` and `AtomicInt` APIs introduced in the Kotlin standard library. On JVM, `javap` shows direct fields of type `java.util.concurrent.atomic.AtomicBoolean` and `AtomicInteger`; `readSide` calls `AtomicBoolean.get()` directly. The old implementation retained boxed `kotlinx.atomicfu.AtomicBoolean`/`AtomicInt` wrappers in the emitted bytecode.

The AtomicFU compiler plugin was explicitly tested and did not scalar-replace Kotlin standard-library atomics, so it was removed rather than retained as a no-op. The non-suspending primitive still uses the AtomicFU runtime only for its common multiplatform `ReentrantLock`, for which Kotlin stdlib has no common equivalent.

The Linux ARM64 fixture cross-compiles and the final ELF disassembly contains:

- `LDARB` for `AtomicBoolean.load()`
- `STLRB` for `AtomicBoolean.store()`
- an `LDAXR`/`STLXR` retry loop for `AtomicInt.fetchAndAdd()`

Those are acquire/release atomic instructions and preserve the required sequentially consistent API behavior. The native padded C counter was also changed from a non-atomic `volatile int` to C11 `_Atomic int` with sequentially consistent load and fetch-add operations.

## Reproduction

Build the JMH jar:

```shell
./gradlew :benchmark:jvmBenchmarkJar
```

Run the core comparison:

```shell
java -jar benchmark/build/benchmarks/jvm/jars/benchmark-jvm-jmh-JMH.jar \
  '.*(CacheMap|ConcurrentHashMap|RWHashMap)(SingleThread|MultiThread).*' \
  -f 2 -wi 3 -w 300ms -i 5 -r 500ms -rf json -rff core.json
```

Run a read-scaling point by adding `-bm thrpt -t <threads>` and selecting the three single-thread benchmark classes' `get` methods. Run the padding controls with `'.*CachePaddingBenchmark.*'`, and the suspend controls with `'.*(CoroutineBridgeBenchmark|SuspendCacheMapBenchmark|SuspendMutexMapBenchmark).*'`.

Build the ARM64 code-generation fixture:

```shell
./gradlew :benchmark:linkAtomicCodegenDebugExecutableLinuxArm64
```

The executable is produced at `benchmark/build/bin/linuxArm64/atomicCodegenDebugExecutable/atomicCodegen.kexe` and can be inspected with `llvm-nm` and `llvm-objdump`.

## Limitations

- Results are from one machine and one JVM. JMH forks reduce but do not eliminate thermal, core-placement, and heterogeneous-core effects.
- Apple native benchmarks were not linked locally because the full Xcode toolchain is not installed. JVM benchmarks ran natively on Apple ARM64, and Kotlin/Native Linux ARM64 was cross-compiled for instruction inspection.
- The 18-thread average-time results report aggregate per-operation timing under synchronized JMH worker starts; they are not end-to-end request latency percentiles.
- Kotlin's common atomic API remains experimental and can introduce binary incompatibility in a future Kotlin release. This project opts in deliberately.
