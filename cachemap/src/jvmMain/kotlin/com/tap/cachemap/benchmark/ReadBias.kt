package com.tap.cachemap.benchmark

@JvmInline
value class ReadBias(val amount: Int) {
    init {
        require(amount in 1..100) {
            "Percentage must be in the range of 1 to 100"
        }
    }
}
