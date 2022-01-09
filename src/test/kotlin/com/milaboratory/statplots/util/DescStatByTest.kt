package com.milaboratory.statplots.util

import org.junit.jupiter.api.Test

/**
 *
 */
internal class DescStatByTest {
    @Test
    internal fun test1() {
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).mean.toList())
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).std.toList())
        println(descStatBy(TestData.toothGrowth, "len", listOf("supp")).err.toList())
    }
}
